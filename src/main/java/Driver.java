import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;


public class Driver {

	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {

		/* represent arguments explicitly */
		/* input and output dir for job 1*/
		String inputDir = args[0];
		String outputDir = args[1];
		/* noGram */
		String numberOfNGram = args[2];

		/* threshold: drop start words + following word combinations with frequency less than threshhold */
		String threshold = args[3];
		/* top n frequent following words */
		String numberOfFollowingWords = args[4];


		/* MapReduce job 1: NGramLibraryBuilder */
		Configuration conf1 = new Configuration();

		/* delimiter */
		conf1.set("textinputformat.record.delimiter", ".");
		conf1.set("noGram", numberOfNGram);

		/* Job: allows users to configure the job, submit job, control its execution and query the state */
		Job job1 = Job.getInstance(conf1);
		job1.setJobName("NGram");
		job1.setJarByClass(Driver.class);

		
		job1.setMapperClass(NGramLibraryBuilder.NGramMapper.class);
		job1.setReducerClass(NGramLibraryBuilder.NGramReducer.class);
		
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(IntWritable.class);
		
		job1.setInputFormatClass(TextInputFormat.class);
		job1.setOutputFormatClass(TextOutputFormat.class);
		
		TextInputFormat.setInputPaths(job1, new Path(inputDir));
		TextOutputFormat.setOutputPath(job1, new Path(outputDir));
		job1.waitForCompletion(true);
		

		
		/* MapReduce job 2: LanguageModel */
		Configuration conf2 = new Configuration();
		conf2.set("threashold", threshold);
		conf2.set("n", numberOfFollowingWords);

		/* Configure jdbcDrive, db user, db password, database
		* Change "ip_address:port" to customized ip
		* Change "password" the cusotmized password for mysql database
		* */
		DBConfiguration.configureDB(conf2, 
				"com.mysql.jdbc.Driver",
				"jdbc:mysql://ip_address:port/test",
				"root",
				"password");
		
		Job job2 = Job.getInstance(conf2);
		job2.setJobName("Model");
		job2.setJarByClass(Driver.class);

		/* Change "path_to_ur_connector" to customized path for the mysql-connector-java*.jar */
		job2.addArchiveToClassPath(new Path("path_to_ur_connector"));

		/*Map output and Output have different types, define separately */
		job2.setMapOutputKeyClass(Text.class);
		job2.setMapOutputValueClass(Text.class);
		job2.setOutputKeyClass(DBOutputWritable.class);
		job2.setOutputValueClass(NullWritable.class);
		
		job2.setMapperClass(LanguageModel.Map.class);
		job2.setReducerClass(LanguageModel.Reduce.class);
		
		job2.setInputFormatClass(TextInputFormat.class);
		job2.setOutputFormatClass(DBOutputFormat.class);

		/* Define the output databased name as "output", colunm names "starting_phrase", ... */
		DBOutputFormat.setOutput(job2, "output", 
				new String[] {"starting_phrase", "following_word", "count"});

		TextInputFormat.setInputPaths(job2, args[1]);
		job2.waitForCompletion(true);
	}

}
