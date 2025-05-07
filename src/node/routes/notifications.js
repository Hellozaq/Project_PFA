const express = require('express');
const router = express.Router();
const notificationController = require('../controllers/notification.controller');

// Get all notifications for the current user
router.get('/', notificationController.getAllNotifications);

// Get notification by ID
router.get('/:id', notificationController.getNotificationById);

// Create a new notification
router.post('/', notificationController.createNotification);

// Mark notification as read
router.patch('/:id/read', notificationController.markNotificationAsRead);

// Delete notification
router.delete('/:id', notificationController.deleteNotification);

module.exports = router;