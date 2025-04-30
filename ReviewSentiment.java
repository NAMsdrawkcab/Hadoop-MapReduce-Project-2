/**
 * This program uses MapReduce to find the overall sentiment of a large dataset of Amazon reviews
 * @author Nam Luu
 * @version 1.0
 */
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ReviewSentiment {
	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable>{
		/**
		 * This determines whether a review is positive, negative, or neutral
		 */
		
		private final static IntWritable one = new IntWritable(1);
	    private Text review = new Text(); //review content
	    private Text sentiment = new Text(); //review sentiment
	    //hashmaps for positive and negative words
	    Map<String, String> positiveWords = new HashMap<String, String>();
        Map<String, String> negativeWords = new HashMap<String, String>();
        
        public void map(Object key, Text value, Context context ) throws IOException, InterruptedException {
	    	int posScore = 0; //positivity score
	        int negScore = 0; //negative score
	        StringTokenizer itr = new StringTokenizer(value.toString(), "\n");
	        
	        //open list of positive words
	        try{
	        	FileReader posF = new FileReader("/home/cloudera/amazonsentiment/words/positive-words.txt");
		        BufferedReader bP = new BufferedReader(posF);
		        if(positiveWords.isEmpty()){
		        	while(bP.ready()){
		        		String newWord = bP.readLine();
		        		positiveWords.put(newWord, newWord);
		        	}
		        }
	        }catch (FileNotFoundException e){
	        	e.printStackTrace();
	        }
	        //open list of negative words
	        try{
	        	FileReader negF = new FileReader("/home/cloudera/amazonsentiment/words/negative-words.txt");
		        BufferedReader bN = new BufferedReader(negF);
		        if(negativeWords.isEmpty()){
		        	while(bN.ready()){
			        	String newWord = bN.readLine();
			        	negativeWords.put(newWord, newWord);
			        }
		        }
	        }catch (FileNotFoundException e){
	        	e.printStackTrace();
	        }
	        
	        while( itr.hasMoreTokens()) {
	        	String[] stuff = itr.nextToken().toString().split("\",\"", 3);
	            review.set( stuff[2] ); //collect review content
	            
		        String word = "";
		        
		        
		        StringTokenizer newItr = new StringTokenizer(review.toString(), "\",.!?\' ");
		        while( newItr.hasMoreTokens() ){
		          	word = newItr.nextToken().toString();
		          	//check if word is positive or negative
		          	if(positiveWords.containsKey(word.toLowerCase())){
		          		posScore++;
		          	}else if(negativeWords.containsKey(word.toLowerCase())){
		          		negScore++;
		          	}
		        }
		    }
		    if(posScore > negScore){
		        sentiment.set("Positive");
		    }
		    else if(negScore > posScore){
	        	sentiment.set("Negative");
		    }
		    else{
		        sentiment.set("Neutral");
	        }
		    posScore = 0;
		    negScore = 0;
	        context.write( sentiment, one ); //set sentiment as key and one as value
	    }
	}

	public static class SentimentReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
		/**
		 * This counts how many positive, negative, and neutral reviews there are
		 */
		private IntWritable result = new IntWritable();

	    public void reduce(Text key, Iterable<IntWritable> values, Context context ) throws IOException, InterruptedException {
	        int sum = 0;

	        for( IntWritable val : values){
	            sum += val.get();
	        }
	        result.set(sum);
	        context.write(key, result);
	    }
	}

	public static void main(String[] args) throws Exception {
	    Configuration conf = new Configuration();
	    Job job = Job.getInstance(conf, "review sentiment");
	    job.setJarByClass(ReviewSentiment.class);
	    job.setMapperClass(TokenizerMapper.class);
	    job.setCombinerClass(SentimentReducer.class);
	    job.setReducerClass(SentimentReducer.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
