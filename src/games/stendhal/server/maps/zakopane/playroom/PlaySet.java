/* $Id: PlaySet.java,v 1.7 2011/06/19 02:28:01 edi18028 Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

package games.stendhal.server.maps.zakopane.playroom;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;

public class PlaySet {
	private StendhalRPZone zone;

	public PlaySet(final StendhalRPZone zone) {
		this.zone = zone;
	}

	public void createFiguraFioletowa(final int x, final int y) {
		final Item figurafioletowa = SingletonRepository.getEntityManager()
				.getItem("figura fioletowa");
		figurafioletowa.setPosition(x, y);
		zone.add(figurafioletowa, false);
	}

	public void createFiguraZielona(final int x, final int y) {
		final Item figurazielona = SingletonRepository.getEntityManager()
				.getItem("figura zielona");
		figurazielona.setPosition(x, y);
		zone.add(figurazielona, false);
	}

	public void createDamkaFioletowa(final int x, final int y) {
		final Item damkafioletowa = SingletonRepository.getEntityManager()
				.getItem("damka fioletowa");
		damkafioletowa.setPosition(x, y);
		zone.add(damkafioletowa, false);
	}

	public void createDamkaZielona(final int x, final int y) {
		final Item damkazielona = SingletonRepository.getEntityManager()
				.getItem("damka zielona");
		damkazielona.setPosition(x, y);
		zone.add(damkazielona, false);
	}

	public void createPionekCzerwony(final int x, final int y) {
		final Item pionekczerwony = SingletonRepository.getEntityManager()
				.getItem("pionek czerwony");
		pionekczerwony.setPosition(x, y);
		zone.add(pionekczerwony, false);
	}

	public void createPionekNiebieski(final int x, final int y) {
		final Item pionekniebieski = SingletonRepository.getEntityManager()
				.getItem("pionek niebieski");
		pionekniebieski.setPosition(x, y);
		zone.add(pionekniebieski, false);
	}

	public void createPionekZielony(final int x, final int y) {
		final Item pionekzielony = SingletonRepository.getEntityManager()
				.getItem("pionek zielony");
		pionekzielony.setPosition(x, y);
		zone.add(pionekzielony, false);
	}

	public void createPionekZolty(final int x, final int y) {
		final Item pionekzolty = SingletonRepository.getEntityManager()
				.getItem("pionek żółty");
		pionekzolty.setPosition(x, y);
		zone.add(pionekzolty, false);
	}

	public void createKostka(final int x, final int y) {
		final Item kostka = SingletonRepository.getEntityManager()
				.getItem("kostka");
		kostka.setPosition(x, y);
		zone.add(kostka, false);
	}

	public void createKolko(final int x, final int y) {
		final Item kolko = SingletonRepository.getEntityManager()
				.getItem("kółko");
		kolko.setPosition(x, y);
		zone.add(kolko, false);
	}

	public void createKrzyzyk(final int x, final int y) {
		final Item krzyzyk = SingletonRepository.getEntityManager()
				.getItem("krzyżyk");
		krzyzyk.setPosition(x, y);
		zone.add(krzyzyk, false);
	}

	public void createCzarnyPionek(final int x, final int y) {
		final Item czarnypionek = SingletonRepository.getEntityManager()
				.getItem("czarny pionek");
		czarnypionek.setPosition(x, y);
		zone.add(czarnypionek, false);
	}

	public void createCzarnaWieza(final int x, final int y) {
		final Item czarnawieza = SingletonRepository.getEntityManager()
				.getItem("czarna wieża");
		czarnawieza.setPosition(x, y);
		zone.add(czarnawieza, false);
	}

	public void createCzarnySkoczek(final int x, final int y) {
		final Item czarnyskoczek = SingletonRepository.getEntityManager()
				.getItem("czarny skoczek");
		czarnyskoczek.setPosition(x, y);
		zone.add(czarnyskoczek, false);
	}

	public void createCzarnyGoniec(final int x, final int y) {
		final Item czarnygoniec = SingletonRepository.getEntityManager()
				.getItem("czarny goniec");
		czarnygoniec.setPosition(x, y);
		zone.add(czarnygoniec, false);
	}

	public void createCzarnyHetman(final int x, final int y) {
		final Item czarnyhetman = SingletonRepository.getEntityManager()
				.getItem("czarny hetman");
		czarnyhetman.setPosition(x, y);
		zone.add(czarnyhetman, false);
	}

	public void createCzarnyKrol(final int x, final int y) {
		final Item czarnykrol = SingletonRepository.getEntityManager()
				.getItem("czarny król");
		czarnykrol.setPosition(x, y);
		zone.add(czarnykrol, false);
	}

	public void createBialyPionek(final int x, final int y) {
		final Item bialypionek = SingletonRepository.getEntityManager()
				.getItem("biały pionek");
		bialypionek.setPosition(x, y);
		zone.add(bialypionek, false);
	}

	public void createBialaWieza(final int x, final int y) {
		final Item bialawieza = SingletonRepository.getEntityManager()
				.getItem("biała wieża");
		bialawieza.setPosition(x, y);
		zone.add(bialawieza, false);
	}

	public void createBialySkoczek(final int x, final int y) {
		final Item bialyskoczek = SingletonRepository.getEntityManager()
				.getItem("biały skoczek");
		bialyskoczek.setPosition(x, y);
		zone.add(bialyskoczek, false);
	}

	public void createBialyGoniec(final int x, final int y) {
		final Item bialygoniec = SingletonRepository.getEntityManager()
				.getItem("biały goniec");
		bialygoniec.setPosition(x, y);
		zone.add(bialygoniec, false);
	}

	public void createBialyHetman(final int x, final int y) {
		final Item bialyhetman = SingletonRepository.getEntityManager()
				.getItem("biały hetman");
		bialyhetman.setPosition(x, y);
		zone.add(bialyhetman, false);
	}

	public void createBialyKrol(final int x, final int y) {
		final Item bialykrol = SingletonRepository.getEntityManager()
				.getItem("biały król");
		bialykrol.setPosition(x, y);
		zone.add(bialykrol, false);
	}
}
