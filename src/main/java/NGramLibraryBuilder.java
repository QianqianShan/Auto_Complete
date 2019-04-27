import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


public class NGramLibraryBuilder {
	public static class NGramMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
		/*
		LongWritable class: a writable class that wraps a java Long type
		Text: Writable class wraps a java string
		Pros of writable class: makes it more convenient to sort (key, value), easier to serialization/deserialization
		                        of data

		LongWritable, Text : input key and value
		Text, IntWritable: output key and value
		*/

		int noGram;
		@Override
		public void setup(Context context) {
			/*
			Configuration: system properties such as default delimiter or some customized properties (e.g., noGram below)
			*/
			Configuration conf = context.getConfiguration();

			noGram = conf.getInt("noGram", 5);
		}

		/*
		map:
		1. generate 2-gram to N-gram
		2. Output key: ngram, n = 2, ..., N
		3. Output value: 1
		* */
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			/*
			input key: offset
			input value: input text
			Context: can be used to write data to disk.
			         Allows the Mapper/Reducer to interact with the rest of the Hadoop system.
			         It includes configuration data for the job as well as interfaces which
			         allow it to emit output.
			         https://stackoverflow.com/questions/26954162/what-is-keyword-context-in-hadoop-programming-world

			Read sentence by sentence instead of line by line (realized by Configuration object) in setup
			*/

			String line = value.toString();

			line = line.trim().toLowerCase();

			/* Replace non-alphabetical characters such as , . ? with a blank space */
			line = line.replaceAll("[^a-z]", " ");

			/* Split words by one or more spaces */
			String[] words = line.split("\\s+");
			/* If there is only one word or no word in a sentence, cannot help constructing ngrams */
			if (words.length < 2) {
				return;
			}

			/* Create mutable string object for saved output
			  http://www.corejavaguru.com/java/strings/stringbuilder

			  write output with 2-gram to N-gram as key and 1 as value
			  For example: with sentence "I love programming and statistics", N = 3
			  The outputs are :
			  I love 1
			  I love programming 1
			  love programming 1
			  love programming and 1
			  ...
			 */
			StringBuilder sb;
			for (int i = 0; i < words.length - 1; i++) {
				sb = new StringBuilder();
				sb.append(words[i]);
				for (int j = 1; i + j < words.length && j < noGram; j++) {
					sb.append(" ");
					sb.append(words[i + j]);
					/* write output pairs to disk */
					context.write(new Text(sb.toString().trim()), new IntWritable(1));
				}
			}
		}
	}

	/* Count the appearances of each n-gram with n = 2, ..., N
	 * Example: "I like \t 200", key and value are separated by \t by default
	 * */
	public static class NGramReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			/*
			 * input key: ngram
			 * input value: <1, 1, 1, ...>
			 * output key: ngram
			 * output value: total count
			 * */

			int count = 0;
			for (IntWritable value: values) {
				count += value.get();
			}
			/* write data */
			context.write(key, new IntWritable(count));

		}
	}

}
