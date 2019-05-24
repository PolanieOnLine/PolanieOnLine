/* $Id$ */
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
package games.stendhal.server.entity.npc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Singleton class that contains inventory and prices of NPC stores.
 */
public final class ShopList {

	static {
		final ShopList shops = ShopList.get();

		//MargaretNPC(semos) i MariaNPC sprzedaje:
		shops.add("food&drinks", "sok z chmielu", 10);
		shops.add("food&drinks", "napój z winogron", 15);
		shops.add("food&drinks", "flasza", 5);
		shops.add("food&drinks", "ser", 20);
		shops.add("food&drinks", "jabłko", 10);
		shops.add("food&drinks", "marchew", 10);
		shops.add("food&drinks", "mięso", 40);
		shops.add("food&drinks", "szynka", 80);
		shops.add("food&drinks", "buteleczka", 8);
		shops.add("food&drinks", "butelka", 10);

		//AdenaNPC sprzedaje:
		shops.add("adosfoodseller", "jabłko", 50);
		shops.add("adosfoodseller", "marchew", 50);

		//SiandraNPC (ados) kupuje:
		shops.add("buyfood", "ser", 5);
		shops.add("buyfood", "mięso", 10);
		shops.add("buyfood", "szpinak", 15);
		shops.add("buyfood", "szynka", 20);
		shops.add("buyfood", "mąka", 25);
		shops.add("buyfood", "borowik", 30);

		//CarmenNPC i IlisaNPC sprzedaje:
		shops.add("healing", "antidotum", 50);
		shops.add("healing", "mały eliksir", 200);
		shops.add("healing", "eliksir", 500);
		shops.add("healing", "duży eliksir", 1000);

		//SarzinaNPC i Kendra MattoriNPC sprzedaje:
		shops.add("superhealing", "antidotum", 50);
		shops.add("superhealing", "mocne antidotum", 150);
		shops.add("superhealing", "eliksir", 500);
		shops.add("superhealing", "duży eliksir", 1000);
		shops.add("superhealing", "wielki eliksir", 1500); //don't want giantheart

		//HaizenNPC i JynathNPC sprzedaje:
		shops.add("scrolls", "zwój semos", 250);
		shops.add("scrolls", "zwój tatrzański", 800);
		shops.add("scrolls", "zwój przywołania", 200);
		shops.add("scrolls", "niezapisany zwój", 2000);

		//Xhiphin ZohosNPC i OrchiwaldNPC sprzedaje:
		shops.add("fadoscrolls", "zwój fado", 600);
		shops.add("fadoscrolls", "niezapisany zwój", 2200);

		//LorithienNPC sprzedaje:
		shops.add("nalworscrolls", "zwój nalwor", 400);
		shops.add("nalworscrolls", "niezapisany zwój", 2000);

		//Mayor ChalmersNPC sprzedaje
		shops.add("adosscrolls", "zwój ados", 400);
		shops.add("adosscrolls", "niezapisany zwój", 2000);

		//HazelNPC sprzedaje:
		shops.add("kirdnehscrolls", "zwój kirdneh", 400);
		shops.add("kirdnehscrolls", "zwój semos", 400);
		shops.add("kirdnehscrolls", "niezapisany zwój", 2000);

		//Erodel BmudNPC w magic city sprzedaje:
		shops.add("allscrolls", "zwój semos", 250);
		shops.add("allscrolls", "zwój przywołania", 200);
		shops.add("allscrolls", "niezapisany zwój", 2000);
		shops.add("allscrolls", "zwój tatrzański", 1000);
		shops.add("allscrolls", "zwój ados", 400);
		shops.add("allscrolls", "zwój nalwor", 400);
		shops.add("allscrolls", "zwój fado", 600);
		shops.add("allscrolls", "zwój kirdneh", 600);

		//Xin BlancaNPC, MichałNPC, WrvilNPC sprzedaje:
		shops.add("sellstuff", "sierp", 25);
		shops.add("sellstuff", "koszula", 25);
		shops.add("sellstuff", "skórzany hełm", 25);
		shops.add("sellstuff", "maczuga", 50);
		shops.add("sellstuff", "sztylecik", 75);
		shops.add("sellstuff", "peleryna", 50);
		shops.add("sellstuff", "skórzane spodnie", 55);
		shops.add("sellstuff", "misiurka", 85);
		shops.add("sellstuff", "mieczyk", 120);
		shops.add("sellstuff", "drewniana tarcza", 100);

		//HagnurkNPC sprzedaje:
		shops.add("sellbetterstuff1", "zbroja lazurowa", 16000);
		shops.add("sellbetterstuff1", "buty lazurowe", 7200);
		shops.add("sellbetterstuff1", "prążkowany płaszcz lazurowy", 13500);
		shops.add("sellbetterstuff1", "lazurowy hełm", 67000);
		shops.add("sellbetterstuff1", "spodnie lazurowe", 6000);
		shops.add("sellbetterstuff1", "lazurowa tarcza", 50000);
		shops.add("sellbetterstuff1", "sztylet mordercy", 80000);

		//GulimoNPC sprzedaje:
		shops.add("sellbetterstuff2", "zbroja cieni", 50000);
		shops.add("sellbetterstuff2", "buty cieni", 26000);
		shops.add("sellbetterstuff2", "płaszcz cieni", 33000);
		shops.add("sellbetterstuff2", "hełm cieni", 12100);
		shops.add("sellbetterstuff2", "spodnie cieni", 22500);
		shops.add("sellbetterstuff2", "tarcza cieni", 60000);
		shops.add("sellbetterstuff2", "piekielny sztylet", 20000);

		//OuchitNPC sprzedaje:
		shops.add("sellrangedstuff", "drewniany łuk", 300);
		shops.add("sellrangedstuff", "strzała", 3);

		//Xin BlancaNPC, MichałNPC, WrvilNPC kupuje:
		shops.add("buystuff", "krótki miecz", 15);
		shops.add("buystuff", "tarcza ćwiekowa", 20);
		shops.add("buystuff", "zbroja ćwiekowa", 22);
		shops.add("buystuff", "hełm nabijany ćwiekami", 17);
		shops.add("buystuff", "spodnie nabijane ćwiekami", 20);
		shops.add("buystuff", "kolczuga", 29);
		shops.add("buystuff", "hełm kolczy", 25);
		shops.add("buystuff", "spodnie kolcze", 27);

		//XoderosNPC sprzedaje:
		shops.add("selltools", "toporek", 15);
		shops.add("selltools", "topór jednoręczny", 95);
		shops.add("selltools", "topór", 110);

		// enable these if you need them for a quest or something
		// shops.add("selltools", "pick", 50);
		// shops.add("selltools", "shovel", 50);
		shops.add("selltools", "pyrlik", 75);
		// used for harvest grain.
		shops.add("selltools", "pordzewiała kosa", 1250);
        // for harvesting cane fields
		shops.add("selltools", "sierp", 80);
		shops.add("selltools", "misa do płukania złota", 230);
		//LorettaNPC w podziemiach orril.dwarfmine kupuje:

		shops.add("buyiron", "żelazo", 75);

		shops.add("buygrain", "kłos", 1);
		//OgnirNPC sprzedaje:
		shops.add("sellrings", "pierścień zaręczynowy", 5000);
		// gold  OgnirNPC skupuje:
		shops.add("buyprecious", "sztabka złota", 250);
		shops.add("buyprecious", "szmaragd", 200);
		shops.add("buyprecious", "szafir", 400);
		shops.add("buyprecious", "rubin", 600);
		shops.add("buyprecious", "diament", 800);
		shops.add("buyprecious", "obsydian", 1000);
		shops.add("buyprecious", "sztabka mithrilu", 2500);

		//McPeglegNPC skupuje:
		// rare weapons shop
		shops.add("buyrare", "bułat", 65);
		shops.add("buyrare", "katana", 70);
		shops.add("buyrare", "berdysz", 75);
		shops.add("buyrare", "złoty pyrlik", 80);

		// rare armor shop
		shops.add("buyrare", "kolczuga wzmocniona", 32);
		shops.add("buyrare", "złota kolczuga", 52);
		shops.add("buyrare", "zbroja płytowa", 62);
		shops.add("buyrare", "tarcza płytowa", 40);
		shops.add("buyrare", "lwia tarcza", 50);

		// rare elf weapons buyer  ElodrinNPC (nalwor) kupuje:
		shops.add("elfbuyrare", "kij", 75);
		shops.add("elfbuyrare", "topór bojowy", 125);
		shops.add("elfbuyrare", "topór obosieczny", 140);
		shops.add("elfbuyrare", "miecz dwuręczny", 110);
		shops.add("elfbuyrare", "pałasz", 135);
		shops.add("elfbuyrare", "wzmocniona lwia tarcza", 245);
		shops.add("elfbuyrare", "tarcza królewska", 280);

		// more rare weapons shop (fado)
		shops.add("buyrare2", "młot bojowy", 120);
		shops.add("buyrare2", "miecz zaczepny", 150);
		shops.add("buyrare2", "kusza", 175);
		shops.add("buyrare2", "półtorak", 250);
		shops.add("buyrare2", "miecz ognisty", 2000);
		shops.add("buyrare2", "miecz lodowy", 5000);
		shops.add("buyrare2", "piekielny sztylet", 8000);

		// very rare armor shop (ados)
		shops.add("buyrare3", "złote spodnie", 3000);
		shops.add("buyrare3", "spodnie cieni", 5000);
		shops.add("buyrare3", "złota zbroja", 7000);
		shops.add("buyrare3", "zbroja cieni", 9000);
		shops.add("buyrare3", "złota tarcza", 10000);
		shops.add("buyrare3", "tarcza cieni", 15000);

		// less rare armor shop (kobold city - kobolds drop some of these
		// things)
		shops.add("buystuff2", "skórzana zbroja łuskowa", 65);
		shops.add("buystuff2", "spodnie nabijane ćwiekami", 70);
		shops.add("buystuff2", "buty nabijane ćwiekami", 75);
		shops.add("buystuff2", "buty kolcze", 100);
		shops.add("buystuff2", "tarcza z czaszką", 100);
		shops.add("buystuff2", "tarcza jednorożca", 125);
		shops.add("buystuff2", "hełm wikingów", 250);

		shops.add("sellstuff2", "buty skórzane", 50);
		shops.add("sellstuff2", "hełm nabijany ćwiekami", 60);
		shops.add("sellstuff2", "tarcza ćwiekowa", 80);
		shops.add("sellstuff2", "miecz", 90);
		shops.add("sellstuff2", "płaszcz krasnoludzki", 230);

		// cloaks shop
		shops.add("buycloaks", "lazurowy płaszcz elficki", 300);
		shops.add("buycloaks", "szmaragdowy płaszcz smoczy", 400);
		shops.add("buycloaks", "lazurowy płaszcz smoczy", 2000);
		shops.add("buycloaks", "karmazynowy płaszcz smoczy", 3000);
		shops.add("buycloaks", "płaszcz cieni", 3000);
		shops.add("buycloaks", "czarny płaszcz smoczy", 4000);
		shops.add("buycloaks", "złoty płaszcz", 5000);
		shops.add("buycloaks", "płaszcz chaosu", 10000);
		shops.add("buycloaks", "czarny płaszcz", 20000);

		// boots shop (mithrilbourgh)
		// Note the shop sign is done by hand in
		// games.stendhal.server.maps.mithrilbourgh.stores
		// Because I wanted to split boots and helmets
		// Please if you change anything, change also the sign (by hand)
		shops.add("boots&helm", "buty żelazne", 1000);
		shops.add("boots&helm", "złote buty", 1500);
		shops.add("boots&helm", "buty cieni", 2000);
		shops.add("boots&helm", "buty kamienne", 2500);
		shops.add("boots&helm", "buty chaosu", 1250);
		shops.add("boots&helm", "buty z zielonego potwora", 6000);
		shops.add("boots&helm", "buty xenocyjskie", 6250);
    shops.add("boots&helm", "hełm xenocyjski", 8000);
		shops.add("boots&helm", "czarne buty", 9000);

		// helmet shop (mithrilbourgh)
		// Note the shop sign is done by hand in
		// games.stendhal.server.maps.mithrilbourgh.stores
		shops.add("boots&helm", "złoty hełm", 3000);
		shops.add("boots&helm", "hełm cieni", 4000);
		shops.add("boots&helm", "złoty hełm wikingów", 5000);
		shops.add("boots&helm", "hełm chaosu", 6000);
		shops.add("boots&helm", "magiczny hełm kolczy", 8000);
		shops.add("boots&helm", "czarny hełm", 10000);

		// buy axes (woodcutter)
		shops.add("buyaxe", "halabarda", 2000);
		shops.add("buyaxe", "topór oburęczny złoty", 4000);
		shops.add("buyaxe", "topór oburęczny magiczny", 6000);
		shops.add("buyaxe", "topór Durina", 8000);
		shops.add("buyaxe", "kosa czarna", 9000);
		shops.add("buyaxe", "topór chaosu", 10000);
		shops.add("buyaxe", "halabarda czarna", 12000);

		// buy chaos items (scared dwarf, after quest)
		shops.add("buychaos", "spodnie chaosu", 8000);
		shops.add("buychaos", "miecz chaosu", 12000);
		shops.add("buychaos", "tarcza chaosu", 18000);
		shops.add("buychaos", "zbroja chaosu", 20000);

		// buy elvish items (albino elf, after quest)
		shops.add("buyelvish", "buty elfickie", 300);
		shops.add("buyelvish", "spodnie elfickie", 300);
		shops.add("buyelvish", "miecz elficki", 800);
		shops.add("buyelvish", "tarcza elficka", 1000);
		shops.add("buyelvish", "miecz elfów ciemności", 1200);
		shops.add("buyelvish", "płaszcz elficki", 400);
		shops.add("buyelvish", "zbroja elficka", 400);

		// magic items or 'relics' (witch in magic city)
		shops.add("buymagic", "miecz demonów", 4000);
		shops.add("buymagic", "sztylet mroku", 8000);
		shops.add("buymagic", "hełm libertyński", 8000);
		shops.add("buymagic", "miecz nieśmiertelnych", 10000);
		shops.add("buymagic", "spodnie nabijane klejnotami", 12000);
		shops.add("buymagic", "magiczna tarcza płytowa", 16000);
		shops.add("buymagic", "magiczna zbroja płytowa", 20000);

		// red items (supplier in sedah city)
		shops.add("buyred", "zbroja karmazynowa", 300);
		shops.add("buyred", "buty karmazynowe", 200);
		shops.add("buyred", "płaszcz karmazynowy", 250);
		shops.add("buyred", "karmazynowy hełm", 200);
		shops.add("buyred", "spodnie karmazynowe", 200);
		shops.add("buyred", "karmazynowa tarcza", 750);

		// mainio items (despot in mithrilbourgh throne room)
		shops.add("buymainio", "zbroja mainiocyjska", 22000);
		shops.add("buymainio", "buty mainiocyjskie", 4000);
		shops.add("buymainio", "płaszcz mainiocyjski", 12000);
		shops.add("buymainio", "hełm mainiocyjski", 8000);
		shops.add("buymainio", "spodnie mainiocyjskie", 7000);
		shops.add("buymainio", "tarcza mainiocyjska", 16000);

		// assassinhq principal Femme Fatale)
		shops.add("buy4assassins", "puklerz", 20);
		shops.add("buy4assassins", "misiurka", 25);
		shops.add("buy4assassins", "kapelusz leśniczego", 30);
		shops.add("buy4assassins", "buty skórzane", 30);
		shops.add("buy4assassins", "płaszcz krasnoludzki", 60);
		shops.add("buy4assassins", "zbroja krasnoludzka", 17000);
		shops.add("buy4assassins", "spodnie krasnoludzkie", 15000);
		shops.add("buy4assassins", "sztylet mordercy", 7000);

		// mountain dwarf buyer of odds and ends -3 ados abandoned keep)
		shops.add("buyoddsandends", "shuriken", 20);
		shops.add("buyoddsandends", "amulet", 800);
		shops.add("buyoddsandends", "czarna perła", 100);
		shops.add("buyoddsandends", "czterolistna koniczyna", 60);
		shops.add("buyoddsandends", "mieczyk", 5);
		shops.add("buyoddsandends", "sztylecik", 20);
		shops.add("buyoddsandends", "pierścień z czaszką", 250);
		shops.add("buyoddsandends", "mocne antidotum", 80);
		shops.add("buyoddsandends", "kolorowe kulki", 80);
		shops.add("buyoddsandends", "zaczarowana igła", 1500);
		shops.add("buyoddsandends", "zima zaklęta w kuli", 150);
		shops.add("buyoddsandends", "gruczoł przędzy", 500);

		// archery shop in nalwor)
		shops.add("buyarcherstuff", "strzała", 2);
		shops.add("buyarcherstuff", "wzmocniona drewniana strzała", 3);
		shops.add("buyarcherstuff", "strzała żelazna", 6);
		shops.add("buyarcherstuff", "strzała złota", 11);
		shops.add("buyarcherstuff", "strzała płonąca", 30);
		shops.add("buyarcherstuff", "strzała z mithrilu", 40);
		shops.add("buyarcherstuff", "drewniany łuk", 200);
		shops.add("buyarcherstuff", "kusza", 400);
		shops.add("buyarcherstuff", "długi łuk", 250);
		shops.add("buyarcherstuff", "klejony łuk", 300);
		shops.add("buyarcherstuff", "kusza łowcy", 800);
		shops.add("buyarcherstuff", "łuk z mithrilu", 80000);

		// selling arrows
		shops.add("sellarrows", "strzała", 3);
		shops.add("sellarrows", "strzała żelazna", 9);
		shops.add("sellarrows", "strzała złota", 18);
		shops.add("sellarrows", "strzała płonąca", 40);

		// assassinhq chief falatheen the dishwasher and veggie buyer)
		// sign is hard coded so if you change this change the sign
		shops.add("buyveggiesandherbs", "marchew", 5);
		shops.add("buyveggiesandherbs", "sałata", 10);
		shops.add("buyveggiesandherbs", "por", 25);
		shops.add("buyveggiesandherbs", "brokuł", 30);
		shops.add("buyveggiesandherbs", "cukinia", 10);
		shops.add("buyveggiesandherbs", "kalafior", 30);
		shops.add("buyveggiesandherbs", "pomidor", 20);
		shops.add("buyveggiesandherbs", "cebula", 20);
		shops.add("buyveggiesandherbs", "arandula", 10);
		shops.add("buyveggiesandherbs", "kokuda", 200);
		shops.add("buyveggiesandherbs", "kekik", 25);
		shops.add("buyveggiesandherbs", "sclaria", 25);

		// gnome village buyer in 0 ados mountain n2 w2)
		shops.add("buy4gnomes", "skórzana zbroja", 25);
		shops.add("buy4gnomes", "maczuga", 3);
		shops.add("buy4gnomes", "skórzany hełm", 15);
		shops.add("buy4gnomes", "peleryna", 25);
		shops.add("buy4gnomes", "jabłko", 5);
		shops.add("buy4gnomes", "kolorowe kulki", 50);
		shops.add("buy4gnomes", "drewniana tarcza", 20);

		// hotdog lady in athor)
		shops.add("buy4hotdogs", "paróweczka", 30);
		shops.add("buy4hotdogs", "kiełbasa serowa", 25);
		shops.add("buy4hotdogs", "chleb", 15);
		shops.add("buy4hotdogs", "cebula", 20);
		shops.add("buy4hotdogs", "prasowany tuńczyk", 15);
		shops.add("buy4hotdogs", "szynka", 15);
		shops.add("buy4hotdogs", "ser", 5);

		shops.add("sellhotdogs", "hotdog", 160);
		shops.add("sellhotdogs", "hotdog z serem", 180);
		shops.add("sellhotdogs", "kanapka z tuńczykiem", 130);
		shops.add("sellhotdogs", "kanapka", 120);
		shops.add("sellhotdogs", "shake waniliowy", 110);
		shops.add("sellhotdogs", "shake czekoladowy", 110);
		shops.add("sellhotdogs", "tabliczka czekolady", 100);
		shops.add("sellhotdogs", "zima zaklęta w kuli", 200);

		// magic city barmaid)
		shops.add("sellmagic", "hotdog", 160);
		shops.add("sellmagic", "hotdog z serem", 180);
		shops.add("sellmagic", "kanapka z tuńczykiem", 130);
		shops.add("sellmagic", "kanapka", 120);
		shops.add("sellmagic", "shake waniliowy", 110);
		shops.add("sellmagic", "shake czekoladowy", 110);
		shops.add("sellmagic", "tabliczka czekolady", 100);
		shops.add("sellmagic", "lukrecja", 100);

		// kirdneh city armor)
		shops.add("buykirdneharmor", "zbroja lazurowa", 13000);
		shops.add("buykirdneharmor", "kamienna zbroja", 18000);
		shops.add("buykirdneharmor", "lodowa zbroja", 19000);
		shops.add("buykirdneharmor", "zbroja xenocyjska", 21000);
		shops.add("buykirdneharmor", "zbroja barbarzyńcy", 5000);
		shops.add("buykirdneharmor", "szmaragdowa tarcza smocza", 13000);
		shops.add("buykirdneharmor", "tarcza xenocyjska", 20000);


		// amazon cloaks shop
		shops.add("buyamazoncloaks", "płaszcz wampirzy", 14000);
		shops.add("buyamazoncloaks", "płaszcz xenocyjski", 18000);
		shops.add("buyamazoncloaks", "peleryna elficka", 50);
		shops.add("buyamazoncloaks", "płaszcz licha", 10000);
		shops.add("buyamazoncloaks", "płaszcz kamienny", 350);
		shops.add("buyamazoncloaks", "prążkowany płaszcz lazurowy", 280);
		shops.add("buyamazoncloaks", "karmazynowy płaszcz smoczy", 4000);
		shops.add("buyamazoncloaks", "kościany płaszcz smoczy", 1500);

		// kirdneh city fishy market)
		shops.add("buyfishes", "okoń", 22);
		shops.add("buyfishes", "makrela", 20);
		shops.add("buyfishes", "płotka", 10);
		shops.add("buyfishes", "palia alpejska", 30);
		shops.add("buyfishes", "błazenek", 30);
		shops.add("buyfishes", "pokolec", 15);
		shops.add("buyfishes", "pstrąg", 45);
		shops.add("buyfishes", "dorsz", 10);

		// semos trading - swords)
		shops.add("tradeswords", "sztylecik", 10);

		// party time! For maria for example. Bit more expensive than normal
		shops.add("sellparty", "napój z oliwką", 100);
		shops.add("sellparty", "tabliczka czekolady", 100);
		shops.add("sellparty", "sok z chmielu", 10);
		shops.add("sellparty", "napój z winogron", 15);
		shops.add("sellparty", "shake waniliowy", 150);
		shops.add("sellparty", "lody", 50);
		shops.add("sellparty", "hotdog", 180);
		shops.add("sellparty", "kanapka", 140);


		// black items (balduin, when ultimate collector quest completed)
		shops.add("buyblack", "czarna zbroja", 60000);
		shops.add("buyblack", "czarne buty", 10000);
		shops.add("buyblack", "czarny płaszcz", 20000);
		shops.add("buyblack", "czarny hełm", 15000);
		shops.add("buyblack", "czarne spodnie", 40000);
		shops.add("buyblack", "czarna tarcza", 75000);
		shops.add("buyblack", "czarny miecz", 20000);
		shops.add("buyblack", "kosa czarna", 40000);
		shops.add("buyblack", "halabarda czarna", 30000);

		// ados market
		shops.add("buyadosarmors", "lazurowa tarcza", 900);

		// Athor ferry
		shops.add("buypoisons", "trucizna", 40);
		shops.add("buypoisons", "muchomor", 60);
		shops.add("buypoisons", "mocna trucizna", 60);
		shops.add("buypoisons", "ryba motyl", 50);
		shops.add("buypoisons", "śmiertelna trucizna", 100);
		shops.add("buypoisons", "bardzo mocna trucizna", 500);
		shops.add("buypoisons", "zabójcza trucizna", 2000);

		// Should have its own shop (buytraps)
		shops.add("buypoisons", "pułapka na gryzonie", 50);

		//Scuba Instructor Edward
		shops.add("sellScubaStuff", "zbroja akwalungowa", 22000);

		// Mine Town Revival Weeks Caroline
		shops.add("sellrevivalweeks", "ciasto z wiśniami", 195);
		shops.add("sellrevivalweeks", "jabłecznik", 195);
		shops.add("sellrevivalweeks", "shake waniliowy", 120);
		shops.add("sellrevivalweeks", "shake czekoladowy", 120);
		shops.add("sellrevivalweeks", "lody", 60);
		shops.add("sellrevivalweeks", "tabliczka czekolady", 100);
		shops.add("sellrevivalweeks", "grillowany stek", 250);
		shops.add("sellrevivalweeks", "hotdog", 170);
		shops.add("sellrevivalweeks", "hotdog z serem", 175);
		shops.add("sellrevivalweeks", "kanapka z tuńczykiem", 140);
		shops.add("sellrevivalweeks", "kanapka", 130);
		shops.add("sellrevivalweeks", "napój z winogron", 25);
		shops.add("sellrevivalweeks", "sok z chmielu", 20);
		shops.add("sellrevivalweeks", "woda", 15);

		// for ados botanical gardens or if you like, other cafes.
		// expensive prices to make sure that the npc production of these items isn't compromised
		shops.add("cafe", "filiżanka herbaty", 80);
		shops.add("cafe", "woda", 50);
		shops.add("cafe", "shake czekoladowy", 150);
		shops.add("cafe", "kanapka", 170);
		shops.add("cafe", "kanapka z tuńczykiem", 180);
		shops.add("cafe", "jabłecznik", 250);

		//bronek kupuje:
		shops.add("buydragonitems", "pazury wilcze", 15);
		shops.add("buydragonitems", "niedźwiedzie pazury", 18);
		shops.add("buydragonitems", "pazury tygrysie", 150);
		shops.add("buydragonitems", "pazur zielonego smoka", 35000);
		shops.add("buydragonitems", "pazur niebieskiego smoka", 45000);
		shops.add("buydragonitems", "pazur arktycznego smoka", 55000);
		shops.add("buydragonitems", "pazur czerwonego smoka", 65000);
		shops.add("buydragonitems", "pazur czarnego smoka", 75000);
		shops.add("buydragonitems", "pazur złotego smoka", 85000);

		//bogus kupuje :
		shops.add("buyskin", "skóra xenocium", 800);
		shops.add("buyskin", "skóra tygrysa", 1000);
		shops.add("buyskin", "skóra lwa", 2000);
		shops.add("buyskin", "skóra zielonego smoka", 2600);
		shops.add("buyskin", "skóra czerwonego smoka", 3200);
		shops.add("buyskin", "skóra niebieskiego smoka", 3800);
		shops.add("buyskin", "skóra arktycznego smoka", 4400);
		shops.add("buyskin", "skóra czarnego smoka", 5000);
		shops.add("buyskin", "skóra złotego smoka", 5600);

		//bogus sprzedaje
		shops.add("sellskin", "buteleczka", 5);
		shops.add("sellskin", "butelka", 7);
		shops.add("sellskin", "krótki miecz", 640);
		shops.add("sellskin", "topór", 1070);
		shops.add("sellskin", "kosa", 2000);

		//npc kupujący pióra...
		shops.add("buypiora", "piórko", 4);
		shops.add("buypiora", "pióro anioła", 3200);
		shops.add("buypiora", "pióro upadłego anioła", 4200);
		shops.add("buypiora", "pióro mrocznego anioła", 5200);
		shops.add("buypiora", "pióro archanioła", 6000);
		shops.add("buypiora", "pióro archanioła ciemności", 7000);
		shops.add("buypiora", "pióro serafina", 11000);
		shops.add("buypiora", "pióro azazela", 14000);

		//zielarka kupuje
		shops.add("buyziola", "arandula", 12);
		shops.add("buyziola", "kokuda", 250);
		shops.add("buyziola", "kekik", 28);
		shops.add("buyziola", "sclaria", 28);

		//kibic sprzedaje
		shops.add("mecz", "bilet na mecz", 250);
		shops.add("mecz", "piłka", 500);

		//krasnolud sprzedaje
		shops.add("sellkopalnia", "kilof", 150);
		shops.add("sellkopalnia", "łopata", 200);
		shops.add("sellkopalnia", "lina", 150);

		//Jagna sprzedaje
		shops.add("urodziny", "sok z chmielu", 8);
		shops.add("urodziny", "napój z winogron", 10);
		shops.add("urodziny", "napój z oliwką", 50);
		shops.add("urodziny", "shake waniliowy", 100);
		shops.add("urodziny", "shake czekoladowy", 100);
		shops.add("urodziny", "mięso", 20);
		shops.add("urodziny", "szynka", 30);
		shops.add("urodziny", "hotdog", 120);
		shops.add("urodziny", "hotdog z serem", 140);
		shops.add("urodziny", "kanapka z tuńczykiem", 110);
		shops.add("urodziny", "kanapka", 110);
		shops.add("urodziny", "tabliczka czekolady", 80);
		shops.add("urodziny", "lukrecja", 80);

		//Wielkolud kupuje
		shops.add("buyrareitems", "kierpce", 100);
		shops.add("buyrareitems", "chusta góralska", 200);
		shops.add("buyrareitems", "cuha góralska", 2000);
		shops.add("buyrareitems", "góralska spódnica", 200);
		shops.add("buyrareitems", "góralski gorset", 300);
		shops.add("buyrareitems", "góralski kapelusz", 300);
		shops.add("buyrareitems", "portki bukowe", 300);
		shops.add("buyrareitems", "polska tarcza lekka", 500);
		shops.add("buyrareitems", "polska tarcza drewniana", 750);
		shops.add("buyrareitems", "polska tarcza kolcza", 1000);
		shops.add("buyrareitems", "polska płytowa tarcza", 2000);
		shops.add("buyrareitems", "polska tarcza ciężka", 2500);
		shops.add("buyrareitems", "korale", 2500);
		shops.add("buyrareitems", "pas zbójecki", 3000);
		shops.add("buyrareitems", "ciupaga", 4000);
		shops.add("buyrareitems", "spinka", 20000);
		shops.add("buyrareitems", "złota ciupaga", 25000);
		shops.add("buyrareitems", "szczerbiec", 1000000);

		//Juhas sprzedaje
		shops.add("juhas", "zwój tatrzański", 250);
		shops.add("juhas", "zwój semos", 300);
		shops.add("juhas", "zwój krakowski", 400);
		shops.add("juhas", "zwój wieliczka", 600);
		shops.add("juhas", "zwój ados", 800);
		shops.add("juhas", "zwój fado", 900);
		shops.add("juhas", "zwój kalavan", 1000);
		shops.add("juhas", "zwój kirdneh", 1000);

		//Stasek sprzedaje
		shops.add("stasek", "wzmocniona drewniana strzała", 4);
		shops.add("stasek", "długi łuk", 1500);
		shops.add("stasek", "peleryna", 25);
		shops.add("stasek", "hełm kolczy", 120);
		shops.add("stasek", "zbroja płytowa", 110);
		shops.add("stasek", "skórzane rękawice", 400);
		shops.add("stasek", "spodnie nabijane ćwiekami", 75);
		shops.add("stasek", "buty żelazne", 240);
		shops.add("stasek", "złoty pyrlik", 355);
		shops.add("stasek", "bułat", 400);
		shops.add("stasek", "topór bojowy", 515);
		shops.add("stasek", "berdysz", 620);
		shops.add("stasek", "pałasz", 740);
		shops.add("stasek", "pordzewiała kosa", 1550);
		shops.add("stasek", "ciupaga", 45000);

		//Radomil sprzedaje
		shops.add("eliksiry", "antidotum", 50);
		shops.add("eliksiry", "mocne antidotum", 250);
		shops.add("eliksiry", "eliksir", 375);
		shops.add("eliksiry", "duży eliksir", 750);
		shops.add("eliksiry", "wielki eliksir", 1250);

		// KRAKOW //
		//Samson sprzedaje:
		shops.add("sellsamson", "toporek", 25);
		shops.add("sellsamson", "topór jednoręczny", 35);
		shops.add("sellsamson", "topór", 50);
		shops.add("sellsamson", "pyrlik", 90);
		shops.add("sellsamson", "pordzewiała kosa", 910);
		shops.add("sellsamson", "misa do płukania złota", 270);

		//StJacek sprzedaje:
		shops.add("sellstjacek", "antidotum", 50);
		shops.add("sellstjacek", "mocne antidotum", 250);
		shops.add("sellstjacek", "mały eliskir", 100);
		shops.add("sellstjacek", "eliksir", 250);
		shops.add("sellstjacek", "duży eliksir", 550);
		shops.add("sellstjacek", "wielki eliksir", 1100);

		//Lajkonik sprzedaje:
		shops.add("selllajkonik", "długi łuk", 1200);
		shops.add("selllajkonik", "wzmocniona drewniana strzała", 6);

		//Idris sprzedaje:
		shops.add("sellidris", "sok z chmielu", 10);
		shops.add("sellidris", "napój z winogron", 15);
		shops.add("sellidris", "flaska", 5);
		shops.add("sellidris", "ser", 20);
		shops.add("sellidris", "marchew", 10);
		shops.add("sellidris", "jabłko", 10);
		shops.add("sellidris", "mięso", 40);
		shops.add("sellidris", "szynka", 80);

		//Merchand sprzedaje:
		shops.add("sellmerchand", "peleryna", 30);
		shops.add("sellmerchand", "sztylecik", 45);
		shops.add("sellmerchand", "spodnie nabijane ćwiekami", 80);
		shops.add("sellmerchand", "katana", 80);
		//Merchand kupi:
		shops.add("buymerchand", "spodnie kolcze", 100);
		shops.add("buymerchand", "buty nabijane ćwiekami", 120);
		shops.add("buymerchand", "skórzana zbroja łuskowa", 150);
		shops.add("buymerchand", "buty kolcze", 180);
		shops.add("buymerchand", "tarcza jednorożca", 200);
		shops.add("buymerchand", "tarcza z czaszką", 225);
		shops.add("buymerchand", "bułat", 250);
		shops.add("buymerchand", "hełm wikingów", 300);

		//Mr Manas kupi:
		shops.add("buymanas", "magia ziemi", 5);
		shops.add("buymanas", "magia płomieni", 7);
		shops.add("buymanas", "magia deszczu", 10);
		shops.add("buymanas", "magia mroku", 14);
		shops.add("buymanas", "magia światła", 16);

		//Winicjusz sprzedaje:
		shops.add("sellwinicjusz", "kiść winogron", 9);

		//Straznik sprzedaje:
		shops.add("wawel", "zwój krakowski", 250);
		shops.add("wawel", "zwój tatrzański", 350);

		//Irek sprzedaje:
		shops.add("sellwarzywairek", "marchew", 5);
		shops.add("sellwarzywairek", "sałata", 10);
		shops.add("sellwarzywairek", "kapusta", 25);
		shops.add("sellwarzywairek", "pomidor", 20);
		shops.add("sellwarzywairek", "cukinia", 30);
		shops.add("sellwarzywairek", "borowik", 35);

		//Zielarka kupuje:
		shops.add("buyziola", "arandula", 12);
		shops.add("buyziola", "kokuda", 250);
		shops.add("buyziola", "kekik", 28);
		shops.add("buyziola", "sclaria", 28);

		//Mysliwy kupuje:
		shops.add("buyzeby", "kieł wilczy", 10);
		shops.add("buyzeby", "kieł niedźwiedzi", 15);
		shops.add("buyzeby", "kieł tygrysi", 35);
		shops.add("buyzeby", "dziób ptaka", 200);
		shops.add("buyzeby", "ząb potwora", 400);
		shops.add("buyzeby", "kieł smoka", 500);
		shops.add("buyzeby", "kieł złotej kostuchy", 5000);

		//Przekupka kupuje:
		shops.add("buywarzywa", "marchew", 2);
		shops.add("buywarzywa", "sałata", 2);
		shops.add("buywarzywa", "pieczarka", 4);
		shops.add("buywarzywa", "opieńka miodowa", 6);
		shops.add("buywarzywa", "pomidor", 5);
		shops.add("buywarzywa", "kapusta", 6);
		shops.add("buywarzywa", "borowik", 6);
		shops.add("buywarzywa", "szpinak", 6);
		shops.add("buywarzywa", "brokuł", 7);
		shops.add("buywarzywa", "por", 7);
		shops.add("buywarzywa", "kalafior", 8);
		shops.add("buywarzywa", "cebula", 8);
		shops.add("buywarzywa", "cukinia", 10);

		//Rybak kupuje:
		shops.add("buyrybak", "dorsz", 5);
		shops.add("buyrybak", "palia alpejska", 7);
		shops.add("buyrybak", "płotka", 7);
		shops.add("buyrybak", "makrela", 8);
		shops.add("buyrybak", "okoń", 8);
		shops.add("buyrybak", "pokolec", 9);
		shops.add("buyrybak", "pstrąg", 9);
		shops.add("buyrybak", "błazenek", 10);

		//Rybak Miłorad kupuje:
		shops.add("buyryby", "dorsz", 6);
		shops.add("buyryby", "palia alpejska", 7);
		shops.add("buyryby", "płotka", 7);
		shops.add("buyryby", "makrela", 8);
		shops.add("buyryby", "okoń", 9);
		shops.add("buyryby", "pokolec", 9);
		shops.add("buyryby", "pstrąg", 9);
		shops.add("buyryby", "błazenek", 11);
		
		//Kacper kupuje:
		shops.add("buykacper", "śnieżka", 5);
		shops.add("buykacper", "kamienie", 6);
		shops.add("buykacper", "bryła lodu", 8);

		//Rzeznik kupuje:
		shops.add("buymieso", "mięso", 15);
		shops.add("buymieso", "szynka", 25);
		shops.add("buymieso", "udko", 12);
		shops.add("buymieso", "stek", 35);

		//Waldek kupuje:
		shops.add("buyowoce", "wisienka", 8);
		shops.add("buyowoce", "jabłko", 5);
		shops.add("buyowoce", "jabłko niezgody", 15);
		shops.add("buyowoce", "poziomka", 5);
		shops.add("buyowoce", "truskawka", 8);
		shops.add("buyowoce", "gruszka", 6);
		shops.add("buyowoce", "kokos", 7);
		shops.add("buyowoce", "ananas", 8);

		//Cheng kupuje:
		shops.add("buycheng", "bryłka mithrilu", 10);
		shops.add("buycheng", "bursztyn", 20);
		shops.add("buycheng", "wielka perła", 1500);
		shops.add("buycheng", "korale", 10000);

		//Farmer Bruno sprzedaje:
		shops.add("sellbruno", "zboże", 4);

		//Thomas sprzedaje:
		shops.add("sellthomas", "tuńczyk", 5);
	}

	private static ShopList instance;

	/**
	 * Returns the Singleton instance.
	 *
	 * @return The instance
	 */
	public static ShopList get() {
		if (instance == null) {
			instance = new ShopList();
		}
		return instance;
	}

	private final Map<String, Map<String, Integer>> contents;

	private ShopList() {
		contents = new HashMap<String, Map<String, Integer>>();
	}

	/**
	 * gets the items offered by a shop with their prices
	 *
	 * @param name name of shop
	 * @return items and prices
	 */
	public Map<String, Integer> get(final String name) {
		return contents.get(name);
	}

	/**
	 * gets a set of all shops
	 *
	 * @return set of shops
	 */
	public Set<String> getShops() {
		return contents.keySet();
	}

	/**
	 * converts a shop into a human readable form
	 *
	 * @param name   name of shop
	 * @param header prefix
	 * @return human readable description
	 */
	public String toString(final String name, final String header) {
		final Map<String, Integer> items = contents.get(name);

		final StringBuilder sb = new StringBuilder(header + "\n");

		for (final Entry<String, Integer> entry : items.entrySet()) {
			sb.append(entry.getKey() + " \t" + entry.getValue() + "\n");
		}

		return sb.toString();
	}

	/**
	 * Add an item to a shop
	 *
	 * @param name the shop name
	 * @param item the item to add
	 * @param price the price for the item
	 */
	public void add(final String name, final String item, final int price) {
		Map<String, Integer> shop;

		if (contents.containsKey(name)) {
			shop = contents.get(name);
		} else {
			shop = new LinkedHashMap<String, Integer>();
			contents.put(name, shop);
		}

		shop.put(item, Integer.valueOf(price));
	}
}
