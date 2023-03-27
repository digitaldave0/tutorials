const express = require('express');
const app = express();
const port = 3000;

const bodyParser = require('body-parser');
app.use(bodyParser.urlencoded({ extended: true }));

const createCsvWriter = require('csv-writer').createObjectCsvWriter;

const csvWriter = createCsvWriter({
  path: 'users.csv',
  header: [
    { id: 'name', title: 'Name' },
    { id: 'dob', title: 'Date of Birth' },
    { id: 'location', title: 'Location' },
  ]
});

app.get('/', (req, res) => {
  res.sendFile(__dirname + '/index.html');
});

app.post('/add-user', (req, res) => {
  const user = req.body;
  
  csvWriter.writeRecords([user])
    .then(() => {
      console.log('User data written to CSV file');
      res.redirect('/');
    })
    .catch((error) => {
      console.error(error);
    });
});

app.listen(port, () => {
  console.log(`Example app listening at http://localhost:${port}`);
});
