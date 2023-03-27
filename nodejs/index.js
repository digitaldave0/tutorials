const express = require('express');
const app = express();
const port = 3000;

app.use(express.urlencoded({ extended: true }));

app.get('/', (req, res) => {
  res.sendFile(__dirname + '/index.html');
});

app.post('/add-user', (req, res) => {
  // Code to handle user data and write to CSV file
});

app.listen(port, () => {
  console.log(`Server listening at http://localhost:${port}`);
});
