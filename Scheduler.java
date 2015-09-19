import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;


public class Scheduler 
{

	
	public static void main(String[] args) 
	{
		Scanner inputFile1;
		Scanner inputFile2;
		ArrayList<Processor> outputTable = new ArrayList<Processor>();
		int currentTime = 0;
		try 
		{
			PrintWriter outputFile = new PrintWriter("output.txt", "UTF-8");
			inputFile1 = new Scanner(new FileReader(args[0]));
			inputFile2 = new Scanner(new FileReader(args[1]));
			int dependencyCount = inputFile1.nextInt();
			
			Node[] dependencies = new Node[dependencyCount+1];
			int[] parents = new int[dependencyCount+1];
			int[] jobTimes = new int[dependencyCount+1];
			int[] jobDone = new int[dependencyCount+1];
			int[] jobTimesRemaining = new int[dependencyCount+1];
			int[] open = new int[dependencyCount+1];
			for(int i=1;i<=dependencyCount;i++)
		    {
		        Node n = new Node(-1);
		        dependencies[i] = n;
		        parents[i] = 0;
		        open[i]=0;
		        jobDone[i] = 0;

		    }
			outputFile.println("dependency count : " + dependencyCount);
			int op1,op2;
			while(inputFile1.hasNext())
			{
				op1 = inputFile1.nextInt();
				op2 = inputFile1.nextInt();
				parents[op1]++;
				Node n = new Node(op2);
				Node walker = dependencies[op1];
				while(walker.next != null)
				{
					walker = walker = walker.next;
				}
				walker.next = n;
			}
			inputFile2.nextInt();
			while(inputFile2.hasNext())
			{
				op1 = inputFile2.nextInt();
				op2 = inputFile2.nextInt();
				jobTimes[op1] = op2;
				jobTimesRemaining[op1] = op2;
			}
			
			
			for(int i=1; i<=dependencyCount;i++)
			{
				outputFile.print("task : " + i + " depends on : ");
				Node walker = dependencies[i];
				while(walker.next != null)
				{
					walker = walker.next;
					outputFile.print(walker.jobId + " ");
				}
				outputFile.println(".");
				outputFile.println("# of parents : " + parents[i]);
				
				outputFile.println("job time : " + jobTimes[i] + " remaining time " + jobTimesRemaining[i]);
				
				
				if(parents[i] > 0) open[i] = -1;
			}
			
			jobLoop(dependencies,jobTimes,jobTimesRemaining,parents,open,outputTable,currentTime,dependencyCount,jobDone,outputFile);
			outputFile.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	private static void jobLoop(Node[] dependencies, int[] jobTimes,
			int[] jobTimesRemaining, int[] parents, int[] open,
			ArrayList<Processor> outputTable, int currentTime,
			int dependencyCount, int[] jobDone, PrintWriter outputFile) 
	{
		outputFile.println(" ");
		boolean DONE = true;
		for(int i = 1; i <= dependencyCount; i++)
		{
			if(jobDone[i] == 0) DONE = false;
		}
		if(DONE) return;
		outputFile.println("current time : " + currentTime);
		ArrayList<Integer> openJobs = findOpenJobs(open,dependencyCount); 
		
		for(int i=0;i < openJobs.size(); i++)
		{
			assignJob(openJobs.get(i),outputTable,currentTime,jobTimes[openJobs.get(i)],outputFile);
			open[openJobs.get(i)] = 1;
		}
		
		for(int i=0;i<outputTable.size();i++)
		{
			ArrayList<Integer> jobs = outputTable.get(i).jobs;
			if(jobs.size() > currentTime)
			{
				if(jobs.get(currentTime) != null)
				{
					int currentProcessedJob = jobs.get(currentTime);
					jobTimesRemaining[currentProcessedJob]--;
					if(jobTimesRemaining[currentProcessedJob] <= 0)
					{
						jobDone[currentProcessedJob] = 1;
						outputFile.write(currentProcessedJob + " Done." + "\n");
						for(int k=1;k<dependencyCount+1;k++)
						{
							Node walker = dependencies[k];
							while(walker != null)
							{
								if(walker.next != null && walker.next.jobId == currentProcessedJob)
								{
									walker.next = walker.next.next;
									parents[k]--;
									outputFile.println("parent reduced for job : " + k + " , dependency removed : " + currentProcessedJob);
									Node walker2 = dependencies[k];
									while(walker2 != null)
									{
										walker2 = walker2.next;
									}
									if(parents[k] < 1) open[k] = 0;
									
								}
								walker = walker.next;
							}
						}
					}
				}
			}
		}
		printout(outputTable,outputFile);
		currentTime++;
		jobLoop(dependencies,jobTimes,jobTimesRemaining,parents,open,outputTable,currentTime,dependencyCount,jobDone,outputFile);
		
	}

	private static void printout(ArrayList<Processor> outputTable,
			PrintWriter outputFile) 
	{
		for(int i=0;i<outputTable.size();i++)
		{
			outputFile.print("p"+i+ " :");
			
			for(int k=0;k < outputTable.get(i).jobs.size();k++)
			{
			
				if(outputTable.get(i).jobs.get(k) == null)
					outputFile.print(" ");
				else
					outputFile.print(outputTable.get(i).jobs.get(k));
			  if(outputTable.get(i).jobs.get(k) != null)
			  {
				if(outputTable.get(i).jobs.get(k) < 10)
					outputFile.print(" ");
				else
					outputFile.print(" ");
			  }
			}
			outputFile.println(" ");
		}
		outputFile.println(" ");
	}

	private static void assignJob(int job,
			ArrayList<Processor> outputTable, int currentTime, int jobLength,
			PrintWriter outputFile) 
	{
		if(outputTable.size() == 0)
		{
			Processor p = new Processor();
			p.jobs = new ArrayList<Integer>(0);
			outputTable.add(p);
			while(jobLength > 0)
			{
				p.jobs.add(job);
				jobLength--;
			}
		}
		else
		{
			boolean isAvailable = false;
			for(int i =0; i < outputTable.size();i++)
			{
				ArrayList<Integer> jobs = outputTable.get(i).jobs;
				if(jobs.size() < currentTime +1)
					while(jobs.size() < currentTime+1)
						jobs.add(null);
				if(jobs.get(currentTime) == null)
				{
					isAvailable = true;
					jobs.set(currentTime,job); jobLength--;
					int futureTime = currentTime +1;
					while(jobLength > 0)
					{
						jobs.add(futureTime, job);
						futureTime++;
						jobLength--;
					}
				break;
					
				}
			}
			if(!isAvailable)
			{
				Processor p = new Processor();
				p.jobs = new ArrayList<Integer>(0);
				outputTable.add(p);
				ArrayList<Integer> jobs = p.jobs;
				while(jobs.size()<currentTime)
					jobs.add(null);
				jobs.add(job); jobLength--;
				int futureTime = currentTime +1;
				while(jobLength>0)
				{
					jobs.add(futureTime,job);
					futureTime++;
					jobLength--;
				}
				
			}
		}
		
	}

	private static ArrayList<Integer> findOpenJobs(int[] open,
			int dependencyCount) 
	{
		ArrayList<Integer> jobs = new ArrayList<Integer>();
		for(int i=1;i<=dependencyCount; i++)
		{
			if(open[i] == 0)
			{
				jobs.add(i);
				open[i] = 1;
			}
		}
		return jobs;
	}

}
