# Projet Intergiciels

Team : Quentin G. & Théo Pirouelle

<a href="https://www.java.com/">
  <img src="https://img.shields.io/badge/language-Java-yellow?style=flat-square" alt="laguage-java" />
</a>

---

The project aims to realize a shared space of typed data. This approach is inspired by the Linda model (or TSpaces). In this model, processes share a space of tuples that they can manipulate using a set of specific primitives.

Afterwards, we have to make new versions.
The first one is to make a shared memory, it is to realize an implementation of the Linda interface which runs directly in the same virtual machine as the user codes.
The second one is a client / mono-server version, it is about realizing an implementation of the Linda interface which accesses a remote server which centralizes the space of tuples.
And the last one is the fault tolerance, we wish to be able to stop and restart the server, by recovering the state of the tuple space, as well as to make the service more resistant. The primary server will be doubled with a backup server.
