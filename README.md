enhanced-common
===

- [Annotations](#annotations)
	- [@Nullable/@NonNull/@NonNullApi/@NonNullFields](#nullablenonnullnonnullapinonnullfields)
- [IO Operations](#io-operations)
	- [HttpClientUtil](#httpclientutil)
	- [FileUtil](#fileutil)
- [Others](#others)
	- [AESUtil](#aesutil)
	- [DigestUtil](#digestutil)
	- [RandomUtil](#randomutil)
	- [Logging](#logging)
- [Type dealing](#type-dealing)
	- [NumberUtil](#numberutil)
	- [StringUtil](#stringutil)
- [Multi Thread](#multi-thread)
	- [GracefullyShutdownThread](#gracefullyshutdownthread)
		- [delayMillis](#delaymillis)
		- [init && deinit](#init--deinit)
		- [shouldNotShutdown](#shouldnotshutdown)
		- [run](#run)
		- [getJobClass](#getjobclass)
		- [call](#call)
	- [QueueDrivenThread](#queuedriventhread)
		- [add](#add)
		- [getQueueSize](#getqueuesize)
		- [getQueueCapacity](#getqueuecapacity)
- [Change Log](#change-log)
	- [1.0.1](#101)
	- [1.0.0](#100)

# Annotations

## @Nullable/@NonNull/@NonNullApi/@NonNullFields
Mark a method/property/parameter possible to be a null or must not be a null

# IO Operations
## HttpClientUtil
* (get/post)Request* related
* Support for connection pool
* Support http/https
* Async Support async*

## FileUtil
* File name dealing related
* UUID generating for naming a file

# Others
## AESUtil
Encrypt and decrypt under AES

## DigestUtil
* Support for MD5/SHA1/SHA2....
* Fast calling `String xx(String)` for md5/sha1/...
* Support file hashing

## RandomUtil
Various random generation supports.

## Logging
Initialize log4j without actual configuration file `log4j2.xml`.
Without spring just call `Logging.initLogging()` and all things done.

# Type dealing
## NumberUtil
Utilities for number type  
* Double compare under specific precision
* Conversion of ip -> long, int -> long, bytes -> long
* Conversion between different systems: Any digits system (2-62 digits) to String, and revert
* Conversion between String or byte[] to long

## StringUtil
Utilities of String  
* ip(string) -> long
* url validation
* email validation
* Conversion of byte[] and hex string
* url encode/decode
* string[] joinning and related
* Conversion of camel and underline format

# Multi Thread
## GracefullyShutdownThread
Abstract thread which can be shutdown gracefully (Under Spring)

### delayMillis
You should call this when want to delay.

### init && deinit
If you override them dont't forget to call the super.init(deinit)

### shouldNotShutdown
It can be overrided to control whether can be shutdown now with maximum timeout 120 seconds.

### run
Don't override this.

### getJobClass
Pass the subclass type to super.

### call
Single run of the actutal logic. It will be called repeatedly.

## QueueDrivenThread

### add
Add item to the inner queue.

### getQueueSize
Fetch the count of current queue size.

### getQueueCapacity
Special the maximum count of the queue. (Called when initializing)

```
add() will throw Exception when the thread is in shutdown process.
```

# Change Log
## 1.0.2
* Add setConnectionValidationPeriod to HttpClientUtil

## 1.0.1
* Fix a bug in `BeansUtil`

## 1.0.0
Initialize for open source


