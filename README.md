# RocketBaseStation

![Demo](https://user-images.githubusercontent.com/12688112/71053199-be5fbb80-211b-11ea-9bd0-936da904a2ce.gif)

# Labels configuration

Labels are configured by the json files inside of `configs`. The default config is `configs/config-hotfireTest.json`

# Usage

`git clone`

Import this as a Gradle project in your preferred IDE.

Setup the labels as described above.

Run `Main.java`

Build with `./gradlew build`

# Command Line Parameters

```bash
--data someDir/someFile.json
```

Specify a different config file from the default `configs/config-hotfireTest.json`

***

```bash
--sim
```

Start in simulation mode

# Simulation

Save your file in the `data` folder with the name `data[NUMBER].txt`. Replace `number` with an index of the data source starting at `0`. Make sure you have labels in `data/config.json` for every data file you have.

# Window Management

Drag charts by dragging them around. Resize charts by dragging at the corners. Snap panels by double clicking on them. They will snap to the largest possible window from the position you clicked. Use the middle mouse button to delete charts.

# How to use

Select a chart, then select any data row to show in the chart. To change the x-axis, use right click.
