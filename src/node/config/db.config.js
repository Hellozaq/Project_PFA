const { Sequelize } = require('sequelize');
const winston = require('winston');

// Configure logger
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  defaultMeta: { service: 'database-service' },
  transports: [
    new winston.transports.Console({
      format: winston.format.combine(
        winston.format.colorize(),
        winston.format.simple()
      )
    }),
    new winston.transports.File({ filename: 'logs/db-error.log', level: 'error' }),
    new winston.transports.File({ filename: 'logs/db.log' })
  ]
});

// Database connection
const sequelize = new Sequelize({
  dialect: 'mysql',
  host: process.env.DB_HOST || 'localhost',
  port: process.env.DB_PORT || 3306,
  database: process.env.DB_NAME || 'spring_node_db',
  username: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || 'root',
  logging: (msg) => logger.debug(msg),
  retry: {
    max: 10, // Maximum retry attempts
    match: [
      /ETIMEDOUT/,
      /ECONNREFUSED/,
      /PROTOCOL_CONNECTION_LOST/,
      /PROTOCOL_ENQUEUE_AFTER_FATAL_ERROR/,
      /SequelizeConnectionError/,
      /SequelizeConnectionRefusedError/,
      /SequelizeHostNotFoundError/,
      /SequelizeHostNotReachableError/,
      /SequelizeInvalidConnectionError/,
      /SequelizeConnectionTimedOutError/
    ],
    backoffBase: 1000, // Initial delay between retries (1 second)
    backoffExponent: 1.5, // Exponential backoff factor
  }
});

const connectDB = async () => {
  let retries = 5;
  while (retries) {
    try {
      await sequelize.authenticate();
      logger.info('MySQL Database connected successfully');
      
      // Sync all models
      await sequelize.sync();
      logger.info('Database models synchronized');
      
      return sequelize;
    } catch (error) {
      retries -= 1;
      if (retries === 0) {
        logger.error(`Database connection error: ${error.message}`);
        process.exit(1);
      }
      logger.warn(`Failed to connect to database. Retries left: ${retries}`);
      // Wait for 5 seconds before retrying
      await new Promise(resolve => setTimeout(resolve, 5000));
    }
  }
};

module.exports = { sequelize, connectDB };