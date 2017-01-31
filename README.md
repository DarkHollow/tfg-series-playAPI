TFG - Series playAPI
====================


Branches
========

## master [![Build Status](https://travis-ci.org/DarkHollow/tfg-series-playAPI.svg?branch=master)](https://travis-ci.org/DarkHollow/tfg-series-playAPI) [![Version](https://img.shields.io/badge/release-v0.1.0-blue.svg?ts=1)](https://github.com/DarkHollow/tfg-series-playAPI/releases/tag/v0.1.0) [![Documentation](https://img.shields.io/badge/doc-v0.1.0-green.svg?ts=1)](#documentation)


##### Actual version `v0.1.0`
[![Release](/docs/rocket.png) v0.1.0](https://github.com/DarkHollow/tfg-series-playAPI/releases/tag/v0.1.0)
Including:
- Extended `Serie` entity implemented
- Search series implemented (search all, search by id, search by name like)
- JSON Views implemented to respond only necessary data
- Do not allow search query of less than 3 characters
- General fixes
- Swagger Documentation (both) updated


##### Base version `v0.0.1`
[![Release](/docs/rocket.png) v0.0.1](https://github.com/DarkHollow/tfg-series-playAPI/releases/tag/v0.0.1)
Including:
- Persistence with MySQL
- Log in TVDB on start up and refresh 12h
- Swagger Documentation running the API
- Swagger Documentation via Github Pages


##### Initial version `v0.0.0`
[![Release](/docs/rocket.png) v0.0.0](https://github.com/DarkHollow/tfg-series-playAPI/releases/tag/v0.0.0)


## develop [![Build Status](https://travis-ci.org/DarkHollow/tfg-series-playAPI.svg?branch=develop)](https://travis-ci.org/DarkHollow/tfg-series-playAPI)

##### sprint 2

##### sprint 1
- [![Merge](/docs/git-merge.png) f1-persistencia](https://github.com/DarkHollow/tfg-series-playAPI/commit/afae5affa2267e11c7a0213d91c4126007203b21) [![Success](/docs/check.png)](https://travis-ci.org/DarkHollow/tfg-series-playAPI/builds/169117350) added persistence with MySQL
- [![Merge](/docs/git-merge.png) f2-loginTVDB](https://github.com/DarkHollow/tfg-series-playAPI/commit/0d770990d298835e057b4ef3279df0230bfa7b0a) [![Success](/docs/check.png)](https://travis-ci.org/DarkHollow/tfg-series-playAPI/builds/171504480) added log in TVDB on start up and refresh every 12h
- [![Merge](/docs/git-merge.png) documentacion-swagger](https://github.com/DarkHollow/tfg-series-playAPI/commit/9049dee28153efd6c02b133f2526655eeb4b3dd7) [![Success](/docs/check.png)](https://travis-ci.org/DarkHollow/tfg-series-playAPI/builds/177389040) added Swagger Documentation at http://localhost:9000/api/doc
- [![Merge](/docs/git-merge.png) documentacion-swagger-offline](https://github.com/DarkHollow/tfg-series-playAPI/commit/5192f336f768d48a8061d292de8adf850ca1a190) [![Success](/docs/check.png)](https://travis-ci.org/DarkHollow/tfg-series-playAPI/builds/177477812) added Swagger Documentation Offline at <http://darkhollow.github.com/tfg-series-playAPI>


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


Documentation
=============

There are two ways to access de API Documentation

### Swagger API Documentation
For this, you need to run the API like in [`How to use it`](#how-to-use-it) section, and then you can access the API Documentation and try all routes browsing

```
http://localhost:9000/api/doc
```

### Swagger Offline API Documentation via Github Pages
To access an Offline version of the Swagger API Documentation without the `try it out` buttons in the routes, you can explore it without downloading or cloning the repository browsing

```
http://darkhollow.github.com/tfg-series-playAPI
```
