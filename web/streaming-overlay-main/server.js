// TEST WEBSOCKET SERVER FOR STREAMING OVERLAY
const PORT = 8080;
const express = require("express");
const app = express();
const server = require("http").Server(app);
const io = require("socket.io")(server, {
  cors: {
    origin: `http://localhost:${PORT}`,
    methods: ["GET", "POST"],
  },
});
const dataSimulator = require("./dataSimulator");

app.use(express.static("public"));

io.on("connection", (socket) => {
  console.log("Socket connection established", socket.id);

  setInterval(() => {
    socket.emit("data", {
      data: dataSimulator.getData(),
    });
  }, 100);
});

server.listen(PORT, () => {
  console.log("listening on port " + PORT);
});

dataSimulator.simulate("./log_rocket_6.txt", 33.33);
