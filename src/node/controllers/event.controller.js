const { Event } = require('../models/event.model');
const { io } = require('../index');

// Get all events
exports.getAllEvents = async (req, res) => {
  try {
    const events = await Event.findAll({
      order: [['createdAt', 'DESC']]
    });
    return res.json(events);
  } catch (error) {
    console.error('Error getting events:', error);
    return res.status(500).json({ message: 'Internal server error' });
  }
};

// Get event by ID
exports.getEventById = async (req, res) => {
  try {
    const event = await Event.findByPk(req.params.id);
    
    if (!event) {
      return res.status(404).json({ message: 'Event not found' });
    }
    
    return res.json(event);
  } catch (error) {
    console.error('Error getting event:', error);
    return res.status(500).json({ message: 'Internal server error' });
  }
};

// Create a new event
exports.createEvent = async (req, res) => {
  try {
    const { type, data, ownerId } = req.body;
    
    if (!type || !data) {
      return res.status(400).json({ message: 'Missing required fields' });
    }
    
    const event = await Event.create({
      type,
      data,
      ownerId: ownerId || req.user.sub
    });
    
    // Emit socket event for real-time updates
    io.emit('event', event);
    
    return res.status(201).json(event);
  } catch (error) {
    console.error('Error creating event:', error);
    return res.status(500).json({ message: 'Internal server error' });
  }
};

// Update event
exports.updateEvent = async (req, res) => {
  try {
    const { type, data } = req.body;
    
    const event = await Event.findByPk(req.params.id);
    
    if (!event) {
      return res.status(404).json({ message: 'Event not found' });
    }
    
    // Check if user has permission to modify this event
    if (event.ownerId !== req.user.sub) {
      return res.status(403).json({ message: 'Access denied' });
    }
    
    event.type = type || event.type;
    event.data = data || event.data;
    await event.save();
    
    // Emit socket event for real-time updates
    io.emit('eventUpdated', event);
    
    return res.json(event);
  } catch (error) {
    console.error('Error updating event:', error);
    return res.status(500).json({ message: 'Internal server error' });
  }
};

// Delete event
exports.deleteEvent = async (req, res) => {
  try {
    const event = await Event.findByPk(req.params.id);
    
    if (!event) {
      return res.status(404).json({ message: 'Event not found' });
    }
    
    // Check if user has permission to delete this event
    if (event.ownerId !== req.user.sub) {
      return res.status(403).json({ message: 'Access denied' });
    }
    
    await event.destroy();
    
    // Emit socket event for real-time updates
    io.emit('eventDeleted', req.params.id);
    
    return res.status(204).send();
  } catch (error) {
    console.error('Error deleting event:', error);
    return res.status(500).json({ message: 'Internal server error' });
  }
};