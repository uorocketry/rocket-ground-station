# RocketBaseStation

![Demo](https://user-images.githubusercontent.com/12688112/71053199-be5fbb80-211b-11ea-9bd0-936da904a2ce.gif)

# Add labels

Make a folder called `data` and a file called `config.json` in data. This is a JSON file. Follow the format from `data-example/config.json`.

Rename the `data-example` folder to `data` and then rename one of the config files to `config.json`.

# Usage

`git clone`

Import this as a Gradle project in your preferred IDE.

Setup the labels as described above.

Run `Main.java`

Build with `./gradlew build`

# Simulation

Save your file in the `data` folder with the name `data[NUMBER].txt`. Replace `number` with an index of the data source starting at `0`. Make sure you have labels in `data/config.json` for every data file you have.

# Window Management

Drag charts by dragging them around. Resize charts by dragging at the corners. Snap panels by double clicking on them. They will snap to the largest possible window from the position you clicked. Use the middle mouse button to delete charts.

# How to use

Select a chart, then select any data row to show in the chart. To change the x-axis, use right click.
