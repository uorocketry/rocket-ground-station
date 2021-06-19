const fs = require("fs");
const readline = require("readline");

const Indexes = {
  latitude: 8,
  longitude: 9,
  altitude: 11,
  velocity: 14,
  accelX: 15,
  accelY: 16,
  accelZ: 17,
  state: 19,
};

let data = {
  altitude: "",
  velocity: "",
  acceleration: "",
  state: "",
  latitude: "",
  longitude: "",
};

async function sleep(millis) {
  return new Promise((resolve) => setTimeout(resolve, millis));
}

const getData = () => {
  return data;
};

// parameters: file path, delay (ms)
const simulate = async (file, delay) => {
  const fileStream = fs.createReadStream(file);

  const rl = readline.createInterface({
    input: fileStream,
    crlfDelay: Infinity,
  });

  for await (const line of rl) {
    const values = line.split(";");

    if (values && values.length === 20) {
      data.altitude = values[Indexes.altitude].toString();
      data.latitude = values[Indexes.latitude].toString();
      data.longitude = values[Indexes.longitude].toString();
      data.state = values[Indexes.state].toString();
      const accelX = parseFloat(values[Indexes.accelX]);
      const accelY = parseFloat(values[Indexes.accelY]);
      const accelZ = parseFloat(values[Indexes.accelZ]);
      data.acceleration = Math.sqrt(
        Math.pow(accelX, 2) + Math.pow(accelY, 2) + Math.pow(accelZ, 2)
      ).toString();
      data.velocity = values[Indexes.velocity].toString();
    }

    await sleep(delay);
  }
};

module.exports = {
  simulate: simulate,
  getData: getData,
};
