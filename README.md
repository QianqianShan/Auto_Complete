
# Implementation of Auto-Complete with MapReduce 

1. [Overview](#overview)

1. [Visualization](#visualization)

1. [Preliminaries](#preliminaries)


1. [N-Gram Model](#n-gram-model)

1. [Discussion](#discussion)

1. [Hadoop Setup](#hadoop-setup)

1. [References](#references)


# Overview 

# Visualization 

# Preliminaries

* Operating system: The operating system is Ubuntu 18.04 LTS (Bionic Beaver).


* Docker container (<https://www.docker.com/why-docker>) version 18.09.5:

Docker enables users to bundle an application together with its preferred execution environment to be executed on a target machine. (<https://hadoop.apache.org/>)

See <https://docs.docker.com/install/> for more details on docker installation. Frequently used commands of docker can be found at <https://docs.docker.com/get-started/>.


* Hadoop cluster is launched within docker containers by following instructions at <https://devhub.io/repos/joway-hadoop-cluster-docker>, 3 containers with 1 master and 2 slaves will be started.

Output by running script `start-container.sh`:

```
start hadoop-master container...
start hadoop-slave1 container...
start hadoop-slave2 container...
```

# N-Gram Model

N-gram model is a language model which predicts the next word from the previous $(N-1)$ words, where N-grame is an N-token sequence of words such as ''I like apple''(trigram), ''New York''(bigram) and so on. The intuition of N-gram model is that we can *approximate* the probability of a word given the entire history by the probability given the last $(N-1)$ words. 

## Notation 

Denote a sequence of $n$ words as $$ w_1^n=w_1,w_2,\cdots,w_n $$ and we have a computer-readable collection of text called *corpus*, $C$, that can be used to count frequencies of words or sequences. Denote the count of N-gram of words $w_1,\cdots,w_N$ as $C(w_1,\cdots,w_N)$. 

## Maximum Likelihood estimation (MLE) of Probability of Next Word 

We obtain the MLE estimate of the probabilities of N-gram model by normalizing the counts from corpus $C$, that is, the ratio of counts of sequence $w_{n-N+1}^{n-1}w_n$ over that of $w_{n-N+1}^{n-1}$,
$$P(w_n|w_{n-N+1}^{n-1})=\frac{C(w_{n-N+1}^{n-1}w_n)}{C(w_{n-N+1}^{n-1})}$$ 


## N-Gram Model Implementation

There are three main steps to implement N-gram model: 

* Calculate the counts of each n-gram for $n=1,2,\cdots,N$ based on raw data stored on hadoop distributed file systems (hdfs)

* Calculate the counts of each following word given the previous word sequence and save the top $k$ frequent following words for each possible combination of previous word sequence into MYSQL database. 

* Visualize the auto-complete process with PHP, MySQL and jQuery as shown at [http://www.bewebdeveloper.com/tutorial-about-autocomplete-using-php-mysql-and-jquery](http://www.bewebdeveloper.com/tutorial-about-autocomplete-using-php-mysql-and-jquery
).


### Optimization of N-Gram Model in MapReduce

In the N-gram model, we predict the next word based on MLE of probabilities of words, this can be simplified by justing checking which word has the highest frequency to show up next given the previous $(N-1)$ words. For example, users input *I like*, and we have
$$C(apple|I like)=500$$ and $$C(banana|I like) = 200$$
as the denominator $C(I like)$ is the same for the above two cases, the frequencies reflect their corresponding probabilities. Then we may want to recommend the trigram sequence as *I like apple* as first.

# Discussion 

* N-gram model is sensitive to the corpus we are using. The probabilities are often an implication of specific facts of the used corpus. 

* N-gram model may assign some N-grams zero probability due to the limited corpus we are using. One can use smoothing methods to assign these kinds of N-grams non-zero probability, for example, Laplace smoothing, Good-Turing discounting and so on. 


## Find IP on Linux Systems

* Method 1: `hostname -I`

* Method 2: `ifconfig | grep inet | grep broadcast`


# References 

1. [https://www.docker.com](https://www.docker.com)
2. [http://hadoop.apache.org/](http://hadoop.apache.org/)
3. [https://devhub.io/repos/joway-hadoop-cluster-docker](https://devhub.io/repos/joway-hadoop-cluster-docker)
4. [Speech and Language Processing, Daniel Jurafsky \& James H. Martin (Chapter 3)](https://web.stanford.edu/~jurafsky/slp3/3.pdf)
5. [MapReduce Tutorial on https://hadoop.apache.org](https://hadoop.apache.org/docs/r1.2.1/mapred_tutorial.html)
