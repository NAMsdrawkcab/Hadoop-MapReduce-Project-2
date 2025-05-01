This program was ran in a Cloudera VM in Orcale VirtualBox.\n
Datasets:\n
  \tAmazon Reviews (input file): https://www.kaggle.com/datasets/kritanjalijain/amazon-reviews/data?select=train.csv\n
  \tPositive Words: https://www.kaggle.com/datasets/prajwalkanade/sentiment-analysis-word-lists-dataset?select=positive-words.txt\n
  \tNegative Words: https://www.kaggle.com/datasets/prajwalkanade/sentiment-analysis-word-lists-dataset?select=negative-words.txt\n

To run code:
1. hadoop fs -mkdir /user/cloudera/amazonsentiment /user/cloudera/amazonsentiment/input
2. hadoop fs -mkdir /user/cloudera/amazonsentiment /user/cloudera/amazonsentiment/words
3. hadoop fs -put train.csv /user/cloudera/amazonsentiment/input
4. hadoop fs -put positive-words.txt /user/cloudera/amazonsentiment/words
5. hadoop fs -put negative-words.txt /user/cloudera/amazonsentiment/words
6. mkdir -p build
7. javac -cp /usr/lib/hadoop/*:/usr/lib/hadoop-mapreduce/* ReviewSentiment.java -d build
8. jar -cvf ReviewSentiment.jar -C build/ .
9. hadoop jar ReviewSentiment.jar ReviewSentiment /user/cloudera/amazonsentiment/input /user/cloudera/amazonsentiment/output
