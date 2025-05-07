const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
const dotenv = require('dotenv');
const jwt = require('jsonwebtoken');
const winston = require('winston');
const helmet = require('helmet');
const compression = require('compression');
const { connectDB } = require('./config/db.config');

// Load environment variables
dotenv.config();

// Initialize Express app
const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  }
});

// Configure logger
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  defaultMeta: { service: 'node-service' },
  transports: [
    new winston.transports.Console({
      format: winston.format.combine(
        winston.format.colorize(),
        winston.format.simple()
      )
    }),
    new winston.transports.File({ filename: 'logs/error.log', level: 'error' }),
    new winston.transports.File({ filename: 'logs/combined.log' })
  ]
});

// Middleware
app.use(cors());
app.use(helmet());
app.use(compression());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Connect to MySQL database
connectDB()
  .then(() => {
    logger.info('MySQL connected successfully');
  })
  .catch(err => {
    logger.error('MySQL connection error:', err);
    process.exit(1);
  });

// JWT Authentication middleware
const authenticateJWT = (req, res, next) => {
  const authHeader = req.headers.authorization;
  
  if (authHeader) {
    const token = authHeader.split(' ')[1];
    
    jwt.verify(token, process.env.JWT_SECRET || '5f2c44c7acafd16be7d2670d9d9a4c3a5d2a49c9ec28db63f0e4f9e5b6a0c3d4', (err, user) => {
      if (err) {
        return res.sendStatus(403);
      }
      
      req.user = user;
      next();
    });
  } else {
    res.sendStatus(401);
  }
};

// Routes
app.get('/', (req, res) => {
  res.json({ message: 'Welcome to Node.js service' });
});

// API Routes
const notificationsRouter = require('./routes/notifications');
const eventsRouter = require('./routes/events');

app.use('/api/notifications', authenticateJWT, notificationsRouter);
app.use('/api/events', authenticateJWT, eventsRouter);

// Socket.io connection
io.on('connection', (socket) => {
  logger.info(`New client connected: ${socket.id}`);
  
  socket.on('subscribe', (room) => {
    socket.join(room);
    logger.info(`Client ${socket.id} joined room: ${room}`);
  });
  
  socket.on('unsubscribe', (room) => {
    socket.leave(room);
    logger.info(`Client ${socket.id} left room: ${room}`);
  });
  
  socket.on('message', (data) => {
    logger.info(`Message received in room ${data.room}: ${data.message}`);
    io.to(data.room).emit('message', data);
  });
  
  socket.on('disconnect', () => {
    logger.info(`Client disconnected: ${socket.id}`);
  });
});

// Health check
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'UP' });
});

// Error handler
app.use((err, req, res, next) => {
  logger.error(`${err.status || 500} - ${err.message} - ${req.originalUrl} - ${req.method} - ${req.ip}`);
  
  res.status(err.status || 500).json({
    message: err.message,
    error: process.env.NODE_ENV === 'development' ? err : {}
  });
});

// Start server
const PORT = process.env.NODE_PORT || 3000;
server.listen(PORT, () => {
  logger.info(`Node.js service running on port ${PORT}`);
});

module.exports = { app, server, io };