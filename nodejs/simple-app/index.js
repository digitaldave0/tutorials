const express = require('express')
const app = express()
const port = 3000

app.set('view engine', 'ejs');
// Set up body parsing middleware
app.use(express.urlencoded({ extended: true }))

// Set up static file serving middleware
app.use(express.static('public'))

// Initialize an empty array to hold the entries
let entries = []

// Set up the GET route for the homepage
app.get('/', (req, res) => {
  res.render('index')
})

// Set up the POST route for form submission
app.post('/', (req, res) => {
  // Extract the name and phone number from the form data
  const name = req.body.name
  const phone = req.body.phone

  const phoneRegex = /^\d{10}$/; // regex for 10-digit phone number
  if (!phoneRegex.test(phone)) {
    res.render('error', { message: 'Invalid phone number. Please enter a 10-digit phone number.' });
    return;
  }

  const entry = { name: name, phone: phone };
  entries.push(entry);
  const entryIndex = entries.length - 1;


  // Push the new entry onto the entries array
  // entries.push({ name: name, phone: phone })



  // Render the show.ejs view with the entries array
  res.render('show', { entries: entries })
})

app.get('/index', (req, res) => {
    res.render('index', { entries: entries });
  });

// Start the server
app.listen(port, () => {
  console.log(`Example app listening at http://localhost:${port}`)
})