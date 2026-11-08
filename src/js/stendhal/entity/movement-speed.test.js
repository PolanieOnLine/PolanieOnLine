"use strict";

const assert = require("node:assert/strict");
const {readFileSync} = require("node:fs");
const path = require("node:path");
const {test} = require("node:test");
const vm = require("node:vm");
const ts = require("typescript");

function load(file, dependencies = {}) {
	const exports = {};
	vm.runInNewContext(ts.transpileModule(readFileSync(path.join(__dirname, file), "utf8"), {
		compilerOptions: {module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022}
	}).outputText, {exports, require: name => dependencies[name] || {}});
	return exports;
}

const {Direction} = load("../util/Direction.ts");
const {ActiveEntity} = load("ActiveEntity.ts", {
	"./Entity": {Entity: class {set(key, value) {this[key] = value;} unset(key) {delete this[key];}}},
	"../util/Direction": {Direction},
	"../util/MathUtil": {MathUtil: {compareDouble: (a, b, diff = 0.001) => Math.abs(a - b) < diff}}
});

function movingEntity() {
	const entity = new ActiveEntity();
	Object.assign(entity, {x: 5, y: 5, _x: 6.2, _y: 5, speed: 1, direction: Direction.RIGHT});
	entity.collidesMap = () => false;
	entity.collidesEntities = () => false;
	return entity;
}

test("speed-only updates change velocity without snapping back to the last reported tile", () => {
	for (const speed of [0.25, 0.5, 1.2, 2]) {
		const entity = movingEntity();
		entity.set("speed", String(speed));
		assert.equal(entity._x, 6.2);
		entity.updatePosition(30);
		assert.ok(Math.abs(entity._x - (6.2 + speed / 10)) < 0.00001);
	}
});

test("speed zero still stops at the authoritative position", () => {
	const entity = movingEntity();
	entity.set("speed", "0");
	assert.equal(entity._x, 5);
	entity.updatePosition(300);
	assert.equal(entity._x, 5);
});

test("continuous walking stays monotonic at fractional, normal and higher speeds", () => {
	for (const speed of [0.25, 0.5, 1, 1.1, 1.2, 2, 2.5]) {
		const entity = movingEntity();
		entity._x = 5;
		entity.set("speed", speed);
		let budget = 0;
		let serverX = 5;
		for (let frame = 0; frame < 300; frame++) {
			const previousX = entity._x;
			if (frame % 10 === 9) {
				budget += speed;
				const steps = Math.floor(budget + 1e-9);
				budget -= steps;
				serverX += steps;
				if (steps > 0) entity.set("x", serverX);
			}
			entity.set("hp", 100 - frame % 10);
			entity.updatePosition(30);
			assert.ok(entity._x >= previousX, `speed ${speed}, frame ${frame}: ${previousX} -> ${entity._x}`);
			assert.ok(Math.abs(entity._x - serverX) < Math.max(2, speed), `speed ${speed} drifted from server`);
		}
	}
});
