# wintership

*edit: I didn't get an interview* :cry:


This repo contains my attempt on two of Shopify's winter internship challenges

## Backend challenge

### To Run
```bash
lein ring server
```

### Cool stuffs
* Use core.async to implement a two-stage (fetch, process customer data) pipeline.

### Current limitations
* No async response (one thread per connection)
* ~~Naive data fetching (`Blocking Get` in series)~~ sovled in [#1](https://github.com/lpan/shopify-wintership-2018/pull/1)

## Data engineering challenge

### To Run
```bash
lein run -m data-challenge.core
```
