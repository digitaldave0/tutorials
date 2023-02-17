FROM jenkins/jenkins:lts

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


USER jenkins

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
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