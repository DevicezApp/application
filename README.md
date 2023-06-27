> **This project is currently in a "very early alpha development" state. _Do not use in a production environment!_**

# DeviceZ application

<img src="https://devicez.de/assets/img/logo.png" width=20% height=20% alt="DeviceZ Logo">

The application source-code, containing both the agent (running on the client-side) and the main server serving all
connected clients, while retaining all necessary information in a database. With it, the front-end application
interacts.

![grafik](https://github.com/DevicezApp/application/assets/38865194/076d3b09-2354-4915-927e-67c15b9ff178)
*Sample output of the console interface*

## Goals of this project

This is a free-time project. I'd like to create a completely open-source and free (as in freedom) device management
system. Many such tools popular in the industry are *very* expensive and often unaffordable, especially for smaller
businesses. Besides costs, many of those tools require a permanent internet connection and rely on a cloud provider.

**The main aims of this project therefore are:**

- open-source and free (code-wise and money-wise)
- always self-hosted (no cloud providers, no big brother who's watching)
- capable of working offline / in a local network only
- able to run on windows and linux

## Current state

What's it capable of?

- starting / restarting / stopping machines
- grouping devices
- schedule actions using crontab expressions

What are the next milestones?

- remote script execution (e. g. windows updates)

There are efforts to create a *decent* web interface ([here](https://github.com/DevicezApp/webapp)) for this application. 
Due to the fact that I'm a *pretty bad* web developer, I'd really appreciate some help here!
Although a web-interface is present, the possibility of controlling the application via CLI will always be maintained. 

**If you're a web developer with spare time on your hands, feel free to contact me!**

## Kudos

This software uses some other open-source libraries, which freed me from a lot of tedious work. I'd like to thank them
here:

- [snf4j](https://github.com/snf4j/snf4j) – Provides an awesome and perfectly simple TCP networking library
- [Javalin](https://github.com/javalin/javalin) – A lightweight and easy-to-use HTTP server, perfect for REST-APIs
- [cron-utils](https://github.com/jmrozanec/cron-utils) - A simple utility for parsing crontab expressions
- [bcrypt](https://github.com/patrickfav/bcrypt) - A utility for hashing password using BCrypt algorithm
- [jsoup](https://github.com/jhy/jsoup) - An awesome HTML parser
- [Simple Java Mail](https://github.com/bbottema/simple-java-mail) - Just simply the best way to send mails
