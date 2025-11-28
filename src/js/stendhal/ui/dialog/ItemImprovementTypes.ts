/***************************************************************************
	*                      (C) Copyright 2024 - PolanieOnLine                 *
	***************************************************************************/
/***************************************************************************
	*                                                                         *
	*   This program is free software; you can redistribute it and/or modify  *
	*   it under the terms of the GNU Affero General Public License as        *
	*   published by the Free Software Foundation; either version 3 of the    *
	*   License, or (at your option) any later version.                       *
	*                                                                         *
	***************************************************************************/

export interface ItemImprovementEntry {
id: number;
name: string;
icon?: string;
improve: number;
max: number;
cost: number;
chance: number;
requirements: {[key: string]: number;};
}
