/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

"use strict";


var marauroa = window.marauroa = window.marauroa || {};
var stendhal = window.stendhal = window.stendhal || {};
stendhal.ui = stendhal.ui || {};


/**
 * game window aka world view
 */
stendhal.ui.gamewindow = {
	/** screen offsets in pixels. */
	offsetX: 0,
	offsetY: 0,
	timeStamp: Date.now(),
	textSprites: [],

	draw: function() {
		var startTime = new Date().getTime();

		if (marauroa.me && document.visibilityState === "visible") {
			if (marauroa.currentZoneName === stendhal.data.map.currentZoneName
				|| stendhal.data.map.currentZoneName === "int_vault"
				|| stendhal.data.map.currentZoneName === "int_adventure_island") {
				var canvas = document.getElementById("gamewindow");
				this.targetTileWidth = 32;
				this.targetTileHeight = 32;
				this.drawingError = false;

				this.ctx = canvas.getContext("2d");
				this.ctx.globalAlpha = 1.0;
				this.adjustView(canvas);
				this.ctx.fillStyle = "black";
				this.ctx.fillRect(0, 0, 10000, 10000);

				var tileOffsetX = Math.floor(this.offsetX / this.targetTileWidth);
				var tileOffsetY = Math.floor(this.offsetY / this.targetTileHeight);

				stendhal.data.map.strategy.render(canvas, this, tileOffsetX, tileOffsetY, this.targetTileWidth, this.targetTileHeight);

				this.drawEntitiesTop();
				this.drawTextSprites();
			}
		}
		setTimeout(function() {
			stendhal.ui.gamewindow.draw.apply(stendhal.ui.gamewindow, arguments);
		}, Math.max((1000/20) - (new Date().getTime()-startTime), 1));

	},


	drawEntities: function() {
		var currentTime = new Date().getTime();
		var time = currentTime - this.timeStamp;
		this.timeStamp = currentTime;
		for (var i in stendhal.zone.entities) {
			var entity = stendhal.zone.entities[i];
			if (typeof(entity.draw) != "undefined") {
				entity.updatePosition(time);
				entity.draw(this.ctx);
			}
		}
	},

	drawEntitiesTop: function() {
		var i;
		for (i in stendhal.zone.entities) {
			if (typeof(stendhal.zone.entities[i].drawTop) != "undefined") {
				stendhal.zone.entities[i].drawTop(this.ctx);
			}
		}
	},

	drawTextSprites: function(ctx) {
		for (var i = 0; i < this.textSprites.length; i++) {
			var sprite = this.textSprites[i];
			var remove = sprite.draw(this.ctx);
			if (remove) {
				this.textSprites.splice(i, 1);
				i--;
			}
		}
	},

	adjustView: function(canvas) {
		// IE does not support ctx.resetTransform(), so use the following workaround:
		this.ctx.setTransform(1, 0, 0, 1, 0, 0);

		// Coordinates for a screen centered on player
		var centerX = marauroa.me["_x"] * this.targetTileWidth + this.targetTileWidth / 2 - canvas.width / 2;
		var centerY = marauroa.me["_y"] * this.targetTileHeight + this.targetTileHeight / 2 - canvas.height / 2;

		// Keep the world within the screen view
		centerX = Math.min(centerX, stendhal.data.map.zoneSizeX * this.targetTileWidth - canvas.width);
		centerX = Math.max(centerX, 0);

		centerY = Math.min(centerY, stendhal.data.map.zoneSizeY * this.targetTileHeight - canvas.height);
		centerY = Math.max(centerY, 0);

		this.offsetX = Math.round(centerX);
		this.offsetY = Math.round(centerY);
		this.ctx.translate(-this.offsetX, -this.offsetY);
	},

	addTextSprite: function(sprite) {
		this.textSprites.push(sprite);
	},

	// Mouse click handling
	onMouseDown: (function() {
		var entity;
		var startX;
		var startY;

		function _onMouseDown(e) {
			var pos = stendhal.ui.html.extractPosition(e);
			if (stendhal.ui.globalpopup) {
				stendhal.ui.globalpopup.close();
			}

			startX = pos.offsetX;
			startY = pos.offsetY;

			var x = pos.offsetX + stendhal.ui.gamewindow.offsetX;
			var y = pos.offsetY + stendhal.ui.gamewindow.offsetY;
			entity = stendhal.zone.entityAt(x, y);
			stendhal.ui.timestampMouseDown = +new Date();

			if (e.type !== "dblclick") {
				e.target.addEventListener("mousemove", onDrag);
				e.target.addEventListener("mouseup", onMouseUp);
				e.target.addEventListener("touchmove", onDrag);
				e.target.addEventListener("touchend", onMouseUp);
			} else if (entity == stendhal.zone.ground) {
				entity.onclick(pos.offsetX, pos.offsetY, true);
			}
		}

		function isRightClick(e) {
			if (+new Date() - stendhal.ui.timestampMouseDown > 300) {
				return true;
			}
			if (e.which) {
				return (e.which === 3);
			} else {
				return (e.button === 2);
			}
		}

		function onMouseUp(e) {
			var pos = stendhal.ui.html.extractPosition(e);
			if (isRightClick(e)) {
				if (entity != stendhal.zone.ground) {
					ui.createSingletonFloatingWindow("Action", new ActionContextMenu(entity), pos.pageX - 50, pos.pageY - 5);
				}
			} else {
				entity.onclick(pos.offsetX, pos.offsetY);
			}
			cleanUp(pos);
			pos.target.focus();
			e.preventDefault();
		}

		function onDrag(e) {
			var pos = stendhal.ui.html.extractPosition(e);
			var xDiff = startX - pos.offsetX;
			var yDiff = startY - pos.offsetY;
			// It's not really a click if the mouse has moved too much.
			if (xDiff * xDiff + yDiff * yDiff > 5) {
				cleanUp(e);
			}
		}

		function cleanUp(e) {
			entity = null;
			e.target.removeEventListener("mouseup", onMouseUp);
			e.target.removeEventListener("mousemove", onDrag);
			e.target.removeEventListener("touchend", onMouseUp);
			e.target.removeEventListener("touchmove", onDrag);
		}

		return _onMouseDown;
	})(),

	onMouseMove: function(e) {
		var pos = stendhal.ui.html.extractPosition(e);
		var x = pos.offsetX + stendhal.ui.gamewindow.offsetX;
		var y = pos.offsetY + stendhal.ui.gamewindow.offsetY;
		var entity = stendhal.zone.entityAt(x, y);
		document.getElementById("gamewindow").style.cursor = entity.getCursor(x, y);
	},

	// ***************** Drag and drop ******************
	onDragStart: function(e) {
		var pos = stendhal.ui.html.extractPosition(e);
		var draggedEntity = stendhal.zone.entityAt(pos.offsetX + stendhal.ui.gamewindow.offsetX,
				pos.offsetY + stendhal.ui.gamewindow.offsetY);

		var img = undefined;
		if (draggedEntity.type === "item") {
			img = stendhal.data.sprites.getAreaOf(stendhal.data.sprites.get(draggedEntity.sprite.filename), 32, 32);
		} else if (draggedEntity.type === "corpse") {
			img = stendhal.data.sprites.get(draggedEntity.sprite.filename);
		} else {
			e.preventDefault();
			return;
		}
		window.event = e; // required by setDragImage polyfil
		e.dataTransfer.setDragImage(img, 0, 0);
		e.dataTransfer.setData("Text", JSON.stringify({
			path: draggedEntity.getIdPath(),
			zone: marauroa.currentZoneName
		}));
	},

	onDragOver: function(e) {
		e.preventDefault(); // Necessary. Allows us to drop.
		e.dataTransfer.dropEffect = "move";
		return false;
	},

	onDrop: function(e) {
		var pos = stendhal.ui.html.extractPosition(e);
		var datastr = e.dataTransfer.getData("Text") || e.dataTransfer.getData("text/x-stendhal");
		if (datastr) {
			var data = JSON.parse(datastr);
			var action = {
				"x": Math.floor((pos.offsetX + stendhal.ui.gamewindow.offsetX) / 32).toString(),
				"y": Math.floor((pos.offsetY + stendhal.ui.gamewindow.offsetY) / 32).toString(),
				"zone" : data.zone
			};
			var id = data.path.substr(1, data.path.length - 2);
			var drop = /\t/.test(id);
			if (drop) {
				action["type"] = "drop";
				action["source_path"] = data.path;
			} else {
				action["type"] = "displace";
				action["baseitem"] = id;
			}

			// if ctrl is pressed, we ask for the quantity
			if (e.ctrlKey) {
				ui.createSingletonFloatingWindow("Quantity", new DropQuantitySelectorDialog(action), pos.pageX - 50, pos.pageY - 25);
			} else {
				marauroa.clientFramework.sendAction(action);
			}
		}
		e.stopPropagation();
		e.preventDefault();
	},

	onContentMenu: function(e) {
		e.preventDefault();
	}

};
