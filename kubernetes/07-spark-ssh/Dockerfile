FROM docker.io/apache/spark:latest

USER root

RUN useradd -d /home/runner -g root -G sudo -m -s /bin/bash runner
RUN echo 'PATH=/opt/spark/bin:/opt/spark/sbin:${PATH}' >> /home/runner/.bashrc
RUN echo 'JAVA_HOME=/opt/java/openjdk' >> /home/runner/.bashrc
RUN echo 'export JAVA_HOME' >> /home/runner/.bashrc
RUN echo 'runner:password' | chpasswd

RUN apt-get update
RUN apt-get install -y openssh-server
RUN mkdir /run/sshd
RUN ssh-keygen -A

