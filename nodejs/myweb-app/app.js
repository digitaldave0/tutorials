// Import the necessary modules
const express = require('express');
const bodyParser = require('body-parser');
const csv = require('csv-parser');
const createCsvWriter = require('csv-writer').createObjectCsvWriter;

// Define the header row for the CSV file
const header = [
  { id: 'name', title: 'Name' },
  { id: 'dob', title: 'Date of Birth' },
  { id: 'location', title: 'Location' }
];

// Create a new CSV file with the headers
const csvWriter = createCsvWriter({
  path: 'data.csv',
  header: header,
  append: true
});

const fs = require('fs');


// Create a new Express application
const app = express();

// Set up the middleware to parse request bodies
app.use(bodyParser.urlencoded({ extended: true }));

// Set up a route to serve the home page
app.get('/', (req, res) => {
  // Read the existing records from the CSV file
  const records = [];
  fs.createReadStream('data.csv')
    .pipe(csv())
    .on('data', (data) => {
      records.push(data);
    })
    .on('end', () => {
      // Render the home page with the existing records
      res.send(`
        <html>
          <head>
            <title>User Records</title>
          </head>
          <body>
            <h1>User Records</h1>
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Date of Birth</th>
                  <th>Location</th>
                </tr>
              </thead>
              <tbody>
                ${records.map((record) => `
                  <tr>
                    <td>${record.name}</td>
                    <td>${record.dob}</td>
                    <td>${record.location}</td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
            <form action="/" method="post">
              <label for="name">Name:</label>
              <input type="text" name="name" id="name" required>
              <br>
              <label for="dob">Date of Birth:</label>
              <input type="date" name="dob" id="dob" required>
              <br>
              <label for="location">Location:</label>
              <input type="text" name="location" id="location" required>
              <br>
              <button type="submit">Add User</button>
            </form>
          </body>
        </html>
      `);
    });
});

// Set up a route to handle form submissions
app.post('/', (req, res) => {
  // Extract the form data
  const { name, dob, location } = req.body;
  // Append the new record to the CSV file
  const csvWriter = createCsvWriter({
    path: 'data.csv',
    header: [
      { id: 'name', title: 'Name' },
      { id: 'dob', title: 'Date of Birth' },
      { id: 'location', title: 'Location' },
    ],
    append: true,
  });
  csvWriter.writeRecords([{ name, dob, location }])
    .then(() => {
      // Redirect to the home page after adding the record
      res.redirect('/');
    });
});

// Set up a route to handle record deletions
app.post('/delete', (req, res) => {
  // Extract the record
// to be deleted from the request body
const { name, dob, location } = req.body;
// Read the existing records from the CSV file
const records = [];
fs.createReadStream('data.csv')
.pipe(csv())
.on('data', (data) => {
records.push(data);
})
.on('end', () => {
// Filter out the record to be deleted
const filteredRecords = records.filter((record) => (
record.name !== name ||
record.dob !== dob ||
record.location !== location
));
// Write the updated records to the CSV file
const csvWriter = createCsvWriter({
path: 'data.csv',
header: [
{ id: 'name', title: 'Name' },
{ id: 'dob', title: 'Date of Birth' },
{ id: 'location', title: 'Location' },
],
append: false,
});
csvWriter.writeRecords(filteredRecords)
.then(() => {
// Redirect to the home page after deleting the record
res.redirect('/');
});
});
});

// Start the server
app.listen(3000, () => {
console.log('Server started on port 3000');
});
