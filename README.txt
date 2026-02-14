
Connected Signs
================

Connected Signs is a Minecraft Forge mod for 1.20.1 that visually connects adjacent and stacked signs by rendering a continuous panel across them.

Project
-------
- Mod id: connected_signs
- Author: lucianowayand
- License: MIT (see LICENSE.txt)

Features
--------
- Connects signs placed next to each other and in vertical stacks.
- Renders an extended panel that spans between connected signs.
- Compatibility-focused: works with vanilla signs, and adds optional support for TerraFirmaCraft (TFC) and Artisanal Florae (AFC) when installed.

Compatibility
-------------
- Minecraft: 1.20.1
- Mod loader: Forge 47.x
- Optional integrations:
  - TFC (mod id: tfc)
  - AFC (mod id: afc)
  - Patchouli (mod id: patchouli)

Install
-------
1. Install Minecraft Forge for 1.20.1.
2. Drop the built jar into your instance's mods folder.

Development
-----------
Run the development client:

   ./gradlew runClient

To work on TFC/AFC integration in dev (adds the optional dev dependencies):

   ./gradlew runClient -Ptfc_enabled=true

Build a release jar:

   ./gradlew build

Notes
-----
- Design notes live in docs/SIGN_CONNECT_IDEAS.md
- This repository is based on the Forge MDK; build and run tasks are Gradle-based.