# DeviceZ application

<img src="https://devicez.de/assets/img/logo.png" width=20% height=20% alt="DeviceZ Logo">

The application source-code, containing both the agent (running on the client-side) and the main server serving all
connected clients, while retaining all necessary information in a database. With it, the front-end application
interacts.

## Goals of this project

This is a free-time project. I'd like to create a completely open-source and free (as in freedom) device management
system. Many such tools popular in the industry are *very* expensive and often unaffordable, especially for smaller
business. Besides costs, many of those tools require a permanent internet connection and rely on a cloud provider.

**The main aims of this project therefore are:**

- open-source and free (code-wise and money-wise)
- always self-hosted (no cloud providers, no big brother who's watching)
- capable of working offline / in a local network only
- able to run on windows and linux

## Current state

Due to the fact I'm a *pretty bad* web developer, I'm currently limited to a console-only user interface. In the way I'd
like to use this software, this is currently sufficient. **If you're a web developer with spare time on your hands, feel
free to contact me!**

Besides questions of user interface, I'd like to achieve the following as a first milestone:

- starting / restarting / stopping machines remotely
    - starting machines remotely would obviously be achieved via wake-on-lan. In a perfect world, the software would
      utilize other running clients in the same network to startup clients unreachable from the servers network
- run server-side predefined scripts on machines remotely (e. g. windows updates)
- task scheduler allowing actions to be triggered automatically

## Kudos

This software uses some other open-source libraries, which freed me from a lot of tedious work. I'd like to thank them
here:

- [snf4j](https://github.com/snf4j/snf4j) – Provides an awesome and perfectly simple TCP networking library
- [Javalin](https://github.com/javalin/javalin) – A lightweight and easy-to-use HTTP server, perfect for REST-APIs