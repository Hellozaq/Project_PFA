const { Notification } = require('../models/notification.model');
const { io } = require('../index');

// Get all notifications for a user
exports.getAllNotifications = async (req, res) => {
  try {
    const userId = req.user.sub;
    const notifications = await Notification.findAll({
      where: { recipient: userId },
      order: [['createdAt', 'DESC']]
    });
    
    return res.json(notifications);
  } catch (error) {
    console.error('Error getting notifications:', error);
    return res.status(500).json({ message: 'Internal server error' });
  }
};

// Get notification by ID
exports.getNotificationById = async (req, res) => {
  try {
    const notification = await Notification.findByPk(req.params.id);
    
    if (!notification) {
      return res.status(404).json({ message: 'Notification not found' });
    }
    
    // Check if user has permission to access this notification
    if (notification.recipient !== req.user.sub) {
      return res.status(403).json({ message: 'Access denied' });
    }
    
    return res.json(notification);
  } catch (error) {
    console.error('Error getting notification:', error);
    return res.status(500).json({ message: 'Internal server error' });
  }
};

// Create a new notification
exports.createNotification = async (req, res) => {
  try {
    const { recipient, type, content, data } = req.body;
    
    if (!recipient || !type || !content) {
      return res.status(400).json({ message: 'Missing required fields' });
    }
    
    const notification = await Notification.create({
      recipient,
      type,
      content,
      data,
      isRead: false
    });
    
    // Emit socket event for real-time updates
    io.to(`user:${recipient}`).emit('notification', notification);
    
    return res.status(201).json(notification);
  } catch (error) {
    console.error('Error creating notification:', error);
    return res.status(500).json({ message: 'Internal server error' });
  }
};

// Mark notification as read
exports.markNotificationAsRead = async (req, res) => {
  try {
    const notification = await Notification.findByPk(req.params.id);
    
    if (!notification) {
      return res.status(404).json({ message: 'Notification not found' });
    }
    
    // Check if user has permission to modify this notification
    if (notification.recipient !== req.user.sub) {
      return res.status(403).json({ message: 'Access denied' });
    }
    
    notification.isRead = true;
    notification.readAt = new Date();
    await notification.save();
    
    return res.json(notification);
  } catch (error) {
    console.error('Error marking notification as read:', error);
    return res.status(500).json({ message: 'Internal server error' });
  }
};

// Delete notification
exports.deleteNotification = async (req, res) => {
  try {
    const notification = await Notification.findByPk(req.params.id);
    
    if (!notification) {
      return res.status(404).json({ message: 'Notification not found' });
    }
    
    // Check if user has permission to delete this notification
    if (notification.recipient !== req.user.sub) {
      return res.status(403).json({ message: 'Access denied' });
    }
    
    await notification.destroy();
    
    return res.status(204).send();
  } catch (error) {
    console.error('Error deleting notification:', error);
    return res.status(500).json({ message: 'Internal server error' });
  }
};