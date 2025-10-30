# MineGui
This overview introduces Dear ImGui inside Minecraft, explains why immediate-mode UI helps mod authors, and points you to the rest of the MineGui developer documentation so you can dive deeper when you are ready.

![MineGui showcase](docs/images/showcase.png)

## Why Dear ImGui in Minecraft?
- ImGui provides an immediate-mode API that lets you describe UI each frame, making debug tools, editors, and live overlays quick to experiment with.
- MineGui bridges ImGui with Fabric/Yarn for Minecraft 1.21.4, taking care of native bindings, render scheduling, and cursor policies so you can focus on widgets, not boilerplate.
- Because everything renders in-game, you can inspect state, tweak data, or build mod tooling without switching to external applications.

## Where to go next
- Read the [developer documentation](docs/introduction.md) for environment requirements, setup, and deeper guides.
- Explore the [MineGui GitHub repository](https://github.com/tytoo/MineGui) to follow ongoing development and contribute fixes or new utilities.
