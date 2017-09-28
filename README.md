TFG - Series playAPI
====================


Branches
========

## master [![Build Status](https://travis-ci.org/DarkHollow/tfg-series-playAPI.svg?branch=master)](https://travis-ci.org/DarkHollow/tfg-series-playAPI) [![Version](https://img.shields.io/badge/release-v0.1.0-blue.svg?ts=1)](https://github.com/DarkHollow/tfg-series-playAPI/releases/tag/v0.1.0) [![Documentation](https://img.shields.io/badge/doc-v0.1.0-green.svg?ts=1)](#documentation)

##### Actual version `v0.10.0`
[![Release](https://darkhollow.github.io/tfg-series-playAPI/rocket.svg) v0.10.0](https://github.com/DarkHollow/tfg-series-playAPI/releases/tag/v0.10.0)
Including:
- Register and login
- TV Show requests
  - Search TV Shows outside the systems and then request them
  - Request counter (information for administration)
- Rating TV Shows
  - Global rating: see
  - Personal rating: see, create, update, delete
- API and database version control with `evolutions`
- Administration panel
  - General statistics summary
  - Requests management: see, accept, reject, delete
  - TV Shows management: see, update, delete, smartphone preview
  - Database version control: see updates, update to a new database version
- Heroku: production version deployed
- Technical changes
  - Role management for normal user and administrator
  - Inheritance implementations to reduce code
  - Refactored routes to be REST`
  - Deleted all files, classes and code not necessary
  - All new implementations and refactorings thinking about the new features
  - Improved `unit tests` and `integration tests`
  - `Play Framework` upgraded to v2.5.12
  - `Swagger UI` upgraded to v3.1.7
- Many improvements and code enhacements
- Many improvements of API responses (work in progress)M


##### Version `v0.1.0`
[![Release](https://darkhollow.github.io/tfg-series-playAPI/rocket.svg) v0.1.0](https://github.com/DarkHollow/tfg-series-playAPI/releases/tag/v0.1.0)
Including:
- Extended `TvShow` entity implemented
- Search tvShows implemented (search all, search by id, search by name like)
- JSON Views implemented to respond only necessary data
- Do not allow search query of less than 3 characters
- General fixes
- Swagger Documentation (both) updated


##### Base version `v0.0.1`
[![Release](https://darkhollow.github.io/tfg-series-playAPI/rocket.svg) v0.0.1](https://github.com/DarkHollow/tfg-series-playAPI/releases/tag/v0.0.1)
Including:
- Persistence with MySQL
- Log in TVDB on start up and refresh 12h
- Swagger Documentation running the API
- Swagger Documentation via Github Pages


##### Initial version `v0.0.0`
[![Release](https://darkhollow.github.io/tfg-series-playAPI/rocket.svg) v0.0.0](https://github.com/DarkHollow/tfg-series-playAPI/releases/tag/v0.0.0)


## develop [![Build Status](https://travis-ci.org/DarkHollow/tfg-series-playAPI.svg?branch=develop)](https://travis-ci.org/DarkHollow/tfg-series-playAPI)

##### sprint 2
- ![Pull Request done](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) Feature 2 - Request TV Show
  - Search for TV Shows in the system
  - Search for TV Shows outside the system that are not in the system
  - Request TV Shows found outside the system that are not in the system
- ![Pull Request done](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) Feature 3 - Register and login
  - JWT
  - Password security
- ![Pull Request done](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) Feature 4 - Administration
  - Administration web with login for only administrators
  - General statistics summary index page
  - Request management - see, accept, reject, delete
- ![Pull Request done](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) Feature 5 - Inheritance and role management
- ![Pull Request done](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) Feature 6 - Administration
  - Improved administration web
  - TV Show management - see, update, delete, smartphone preview
- ![Pull Request done](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) Feature 7 - Requests counter and improvements
- ![Pull Request done](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) Feature 8 - Database version control
- ![Pull Request done](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) Feature 9 - Heroku deploy
- ![Pull Request done](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) Feature 10 - Rate TV Shows
  - Global rating - see
  - Personal rating - see, create, update, delete
- ![Pull Request done](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) Many fixes
- ![Pull Request done](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) Refactored routes and controllers functions to be `REST`
- ![Pull Request done](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) Upgraded Play Framework and Swagger UI


##### sprint 1
- [![Merge](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) f1-persistencia](https://github.com/DarkHollow/tfg-series-playAPI/commit/afae5affa2267e11c7a0213d91c4126007203b21) [![Success](https://darkhollow.github.io/tfg-series-playAPI/check-green.svg)](https://travis-ci.org/DarkHollow/tfg-series-playAPI/builds/169117350) added persistence with MySQL
- [![Merge](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) f2-loginTVDB](https://github.com/DarkHollow/tfg-series-playAPI/commit/0d770990d298835e057b4ef3279df0230bfa7b0a) [![Success](https://darkhollow.github.io/tfg-series-playAPI/check-green.svg)](https://travis-ci.org/DarkHollow/tfg-series-playAPI/builds/171504480) added log in TVDB on start up and refresh every 12h
- [![Merge](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) documentacion-swagger](https://github.com/DarkHollow/tfg-series-playAPI/commit/9049dee28153efd6c02b133f2526655eeb4b3dd7) [![Success](https://darkhollow.github.io/tfg-series-playAPI/check-green.svg)](https://travis-ci.org/DarkHollow/tfg-seriews-playAPI/builds/177389040) added Swagger Documentation at http://localhost:9000/api/doc
- [![Merge](https://darkhollow.github.io/tfg-series-playAPI/pull-request-green.svg) documentacion-swagger-offline](https://github.com/DarkHollow/tfg-series-playAPI/commit/5192f336f768d48a8061d292de8adf850ca1a190) [![Success](https://darkhollow.github.io/tfg-series-playAPI/check-green.svg)](https://travis-ci.org/DarkHollow/tfg-series-playAPI/builds/177477812) added Swagger Documentation Offline at <http://darkhollow.github.com/tfg-series-playAPI>


## experiment [![Build Status](https://travis-ci.org/DarkHollow/tfg-series-playAPI.svg?branch=experiment)](https://travis-ci.org/DarkHollow/tfg-series-playAPI)

- /experimentos

  Shows a list of runnable experiments


How to use it
=============
You can simply use this API downloading or cloning this repository and execute in command line

```bash
$ activator run
```

Then, the API is accesible via

```
http://localhost:9000/api
```


Heroku App
==========

You can visit and use the last release on Heroku

https://trending-series-api.herokuapp.com


Documentation
=============

There are two ways to access de API Documentation

### Swagger API Documentation
For this, you need to run the API like in [`How to use it`](#how-to-use-it) section, and then you can access the API Documentation and try all routes browsing

```
http://localhost:9000/api/docs
```

### Swagger offline API Documentation via Github Pages
To access an `offline` version of the Swagger API Documentation without the `try it out` buttons in the routes, you can explore it without downloading or cloning the repository browsing

http://darkhollow.github.com/tfg-series-playAPI
