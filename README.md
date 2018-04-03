# mylizzie
![screenshot](/doc/mylizzie_screen.png)

A go game analyzer based on [Leela Zero](https://github.com/gcp/leela-zero). Derived from [CamWagner's Lizzie](https://github.com/CamWagner/lizzie). Licensed under GPL v3. 

Thanks for [cngoodboy](https://github.com/cngoodboy/lizzie) for some code contributions. 

Thanks for toomasr for his [sgf4j](https://github.com/toomasr/sgf4j) project. This project contains some of his codes.

## How to build
You need [Apache Maven](http://maven.apache.org/). After maven set up, go to the source directory and type
```
mvn package
```
And you will get the built jar with its dependencies in target/distribution directory.

Note that this only builds the executable jar. In order to make mylizzie work, you need to get the other binaries from [Lizzie](https://github.com/CamWagner/lizzie).
Download an official lizzie release and copy mylizzie's jar and lib into the offical lizzie release and happy to use it.
