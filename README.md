# wintership

## Problem description
[https://backend-challenge-winter-2017.herokuapp.com/](https://backend-challenge-winter-2017.herokuapp.com/)

## To Run
```bash
lein ring server
```

## Current limitations
* No async response (one thread per connection)
* ~~Naive data fetching (`Blocking Get` in series)~~ sovled in [#1](https://github.com/lpan/shopify-wintership-2018/pull/1)
