
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

public class LanguageModel {
	public static class Map extends Mapper<LongWritable, Text, Text, Text> {
		/*
		LongWritable class: a writable class that wraps a java Long type
		Text: Writable class wraps a java string
		Pros of writable class: makes it more convenient to sort (key, value), easier to serialization/deserialization
		                        of data
		*/

		int threashold;

		@Override
		public void setup(Context context) {
			/* set up threshold in Configuration */
			Configuration conf = context.getConfiguration();
			threshold = conf.getInt("threshold", 20);
		}

		/*
		* Split each ngram with the first (n-1)-gram as start words, the last gram as following word
		* Input: key value pairs of ngram, count from NGramLibraryBuilder
		* Output key: start words
		* Output value: following words + count
		* */
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			/*
			Context: can be used to write data to disk.
			         Allows the Mapper/Reducer to interact with the rest of the Hadoop system.
			         It includes configuration data for the job as well as interfaces which
			         allow it to emit output.
			         https://stackoverflow.com/questions/26954162/what-is-keyword-context-in-hadoop-programming-world


			*/
			if((value == null) || (value.toString().trim()).length() == 0) {
				return;
			}
			/* line example: I like programming \t 200 */
			String line = value.toString().trim();

			/*wordsPlusCount: "I like programming", "200" */
			String[] wordsPlusCount = line.split("\t");
			if(wordsPlusCount.length < 2) {
				return;
			}

			/* start words + following word */
			String[] words = wordsPlusCount[0].split("\\s+");

			/* count */
			int count = Integer.valueOf(wordsPlusCount[1]);

			/* drop infrequent combinations */
			if (count < threshold) {
				return;
			}

			/* Create key value pairs for output */
			StringBuilder sw = new StringBuilder();
			/* start words */
			for (int i = 0; i < words.length - 1; i++) {
				sw.append(words[i]).append(" ");
			}

			String outputKey = sw.toString().trim();
			String  outputValue = words[words.length - 1] + "=" + count;

			/* write */
			if ( outputKey != null && outputKey.length() >= 1 ) {
				context.write(new Text(outputKey), new Text(outputValue));
			}
		}
	}


	public static class Reduce extends Reducer<Text, Text, DBOutputWritable, NullWritable> {

		int n;
		// get the n parameter from the configuration
		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();
			n = conf.getInt("n", 5);
		}

        /*
        * 1. Find top n frequent following words of each key (start words)
        * 2. Write to database (start words, following words, count)
        *
        * Use TreeMap to save data in the form of (count as TreeMap key, list of following words which appears count times)
        *
        * Input key: start words, e.g., I like
        * Input values: iterables with item like <programming=200, apple=300,...>
        * Output: top n combinations of (input key (start words), following word, count )
        * */
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			/* reverse order the counts  */
			TreeMap<Integer, List<String>> tm = new TreeMap<Integer, List<String>>(Collections.reverseOrder());

			/* Build MapTree */
			for (Text value: values) {
				String curValue = value.toString().trim();
				/*split apple=200 to apple, 200 */
				String word = curValue.split('=')[0].trim();
				int count = curValue.split("=")[1].trim();

				/* add count as key add word into list */
				if (tm.containsKey(count)) {
					tm.get(count).add(word);
				} else {
					List<String> list = new ArrayList<String>();
					list.add(word);
					tm.put(count, list);
				}
			}

            /* write the top n frequent following words in reverse order (most frequent comes first) */
			/* iter: counts of the TreeMap */
			Iterator<Integer> iter = tm.keySet().iterator();
			for (int i = 0; iter.hasNext() && i < n) {
				int count = iter.next();
				List<String> words = tm.get(count);
				for (String word: words) {
					/* write: start words, following word, count
					* Writable class requires key, value pairs, we have written all info in key, so use
					* NullWritable as placeholder for value
					* */
					context.write(new DBOutputWritable(key.toString(), word, count), NullWritable.get());
					i++;
				}
			}
		}
	}
}
