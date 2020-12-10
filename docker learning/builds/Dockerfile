FROM jenkins/jenkins:lts
LABEL version="0.1"
USER root


GOPATH=~/go/
export PATH=$GOPATH/bin:$PATH
ENV HOME=GOPATH=~/go/ \
    URL='https://golang.org/dl/go1.15.5.linux-amd64.tar.gz' \
    FILE='go1.15.5.linux-amd64.tar.gz' 


RUN apt-get update && \
    apt-get -y install \
    apt-transport-https \
    ca-certificates \
    gnupg2 \
    software-properties-common

RUN wget  $URL  -O /tmp/$FILE && \
    tar -xzvf /tmp/$FILE -C /usr/local

docker run -it --rm devenvironment:1.0 bash