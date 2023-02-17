# build docker jenkins image

```
echo -e "FROM jenkins/jenkins:lts

USER root

RUN apt-get update && \
apt-get -y install apt-transport-https \
     ca-certificates \
     curl \
     gnupg2 \
     software-properties-common && \
curl -fsSL https://download.docker.com/linux/$(. /etc/os-release; echo "$ID")/gpg > /tmp/dkey; apt-key add /tmp/dkey && \
add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/$(. /etc/os-release; echo "$ID") \
   $(lsb_release -cs) \
   stable" && \
apt-get update && \
apt-get -y install docker-ce

RUN apt-get install -y docker-ce

RUN usermod -a -G docker jenkins

USER jenkins" > Dockerfile

docker build .
docker login --username <docker_logon_name>
docker images
docker tag ac543a9fa523 <docker_login_name>/my_jenkins_build:1.0
docker push <docker_login_name>/my_jenkins_build
docker run --name jenkins-docker -p 8080:8080 -v /var/run/docker.sock:/var/run/docker.sock <docker_login_name>/my_jenkins_build
```

# running docker 

```
# Remove All old images

docker rmi $(docker images -a -q) -f 
docker rm $(docker ps -aq)
docker images
docker ps

# Build Tag Image in this case Jenkins
cd jenkins
docker build -t myjenkins:1 .
# Run Image
docker run -d --name jenkins-docker -p 8080:8080 -v /var/run/docker.sock:/var/run/docker.sock myjenkins:1

# Get Jenkins key
docker exec -it 59c080a8df24 cat /var/jenkins_home/secrets/initialAdminPassword
```
