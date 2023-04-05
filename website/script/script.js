// script to show/hide work experience details when job title is clicked
const jobTitles = document.querySelectorAll('.work-experience h3');
jobTitles.forEach(function(jobTitle) {
  jobTitle.addEventListener('click', function() {
    const jobDescription = this.nextElementSibling;
    if (jobDescription.style.display === 'block') {
      jobDescription.style.display = 'none';
    } else {
      jobDescription.style.display = 'block';
    }
  });
});

// script to show a progress bar for each skill
const skills = document.querySelectorAll('.skills li');
skills.forEach(function(skill) {
  const skillValue = skill.dataset.value;
  const progressBar = document.createElement('div');
  progressBar.classList.add('progress-bar');
  progressBar.style.width = skillValue + '%';
  skill.appendChild(progressBar);
});

