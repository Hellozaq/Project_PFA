const { DataTypes } = require('sequelize');
const { sequelize } = require('../config/db.config');

const Event = sequelize.define('Event', {
  id: {
    type: DataTypes.UUID,
    defaultValue: DataTypes.UUIDV4,
    primaryKey: true
  },
  type: {
    type: DataTypes.STRING,
    allowNull: false,
    index: true
  },
  data: {
    type: DataTypes.JSON,
    allowNull: false
  },
  ownerId: {
    type: DataTypes.STRING,
    allowNull: false,
    index: true
  }
}, {
  timestamps: true
});

module.exports = Event;