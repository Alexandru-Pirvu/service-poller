const express = require('express');
const app = express();
const ports = [...Array(100).keys()].map(index => index + 3000);

const http = require('http');

app.get('/', (req, res) => {
    const fullUrl = `${req.protocol}://${req.hostname}:${req.socket.localPort}${req.url}`;

    const status = Math.random() < 0.8 ? 200 : 500;
    res.status(status).send(`Mock server with address ${fullUrl} responded with ${status}`);
});

ports.forEach(port => {
    http.createServer(app).listen(port, () => {
        console.log(`Mock server listening on port ${port}`);
    });
});
