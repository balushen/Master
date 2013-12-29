package com.pocketscience;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/***
 * Class for keeping info for the system min, max and curent frequency, number of cpu cores, application binary interface version
 * 
 * @author Pesho
 */
public class SystemInfo
{
    private List<Integer> maxFreq;
    private List<Integer> minFreq;
    private List<Integer> curFreq;
    private int numCores;
    private Integer totalRAM; // in Mb
    private String abi;

    public List<Integer> getMaxFreq()
    {
	return maxFreq;
    }

    public void setMaxFreq(List<Integer> maxFreq)
    {
	this.maxFreq = maxFreq;
    }

    public List<Integer> getMinFreq()
    {
	return minFreq;
    }

    public void setMinFreq(List<Integer> minFreq)
    {
	this.minFreq = minFreq;
    }

    public List<Integer> getCurFreq()
    {
	return curFreq;
    }

    public void setCurFreq(List<Integer> curFreq)
    {
	this.curFreq = curFreq;
    }

    public int getNumCores()
    {
	return numCores;
    }

    public void setNumCores(int numCores)
    {
	this.numCores = numCores;
    }

    public Integer getTotalRAM()
    {
	return totalRAM;
    }

    public void setTotalRAM(Integer totalRAM)
    {
	this.totalRAM = totalRAM;
    }

    public String getAbi()
    {
	return abi;
    }

    public void setAbi(String abi)
    {
	this.abi = abi;
    }

    public SystemInfo()
    {
	maxFreq = new ArrayList<Integer>();
	minFreq = new ArrayList<Integer>();
	curFreq = new ArrayList<Integer>();
	numCores = 0;
	totalRAM = 0;
	abi = "";
    }

    /***
     * Converts SystemInfo object to String
     * 
     * @return returns String variable
     */
    public String toString()
    {
	String result = "";

	result = "ABI: " + this.abi;
	result += "\nTotal system RAM: " + this.totalRAM + " Mb";
	result += "\nNumber of Cores: " + this.numCores;
	//for (int i = 0; i < this.numCores; i++)
	for (int i = 0; i < 1; i++)
	{
	    result += "\n===============================\n";
	   // result += "\nCORE " + (i + 1);
	    result += "\nFrequency range: " + this.minFreq.get(i) / 1000 + "MHz - " + this.maxFreq.get(i) / 1000 + "MHz";
	    result += "\nCurrent frequency: " + this.curFreq.get(i) / 1000 + "MHz";
	}
	return result;
    }

    /***
     * Gets the system info containing all information for all cores
     */
    public void getInfo()
    {
	numCores = numCores();
	// for (int i = 0; i < numCores; i++)
	for (int i = 0; i < 1; i++) //get frequency only for core 0
	{
	    maxFreq.add(i, getMaxFreq(i));
	    minFreq.add(i, getMinFreq(i));
	    curFreq.add(i, getCurFreq(i));
	}
	abi = abi();
	totalRAM = (Integer.parseInt(totalRAM()) / 1024);
    }

    /**
     * Gets the number of cores available in this device, across all processors. Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * 
     * @return The number of cores, or 1 if failed to get result
     */
    public int numCores()
    {
	// Private Class to display only CPU devices in the directory listing
	class CpuFilter implements FileFilter
	{
	    @Override
	    public boolean accept(File pathname)
	    {
		if (Pattern.matches("cpu[0-9]", pathname.getName())) // Check if filename is "cpu" followed by a single digit number
		{
		    return true;
		}
		return false;
	    }
	}
	try
	{
	    File dir = new File("/sys/devices/system/cpu/"); // Get directory containing CPU info
	    File[] files = dir.listFiles(new CpuFilter()); // Filter to only list the devices we care about
	    return files.length; // Return the number of cores (virtual CPU devices)
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    return 1; // Default to return 1 core
	}
    }

    /***
     * Gets the maximum frequency of current cpu core provided as parameter. Requires: Ability to read the system file "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"
     * 
     * @return The max frequency in Hz or 0 if failed to get result
     */
    public int getMaxFreq(int core)
    {
	int maxFreq = 0;
	String line = null;
	try
	{
	    BufferedReader reader = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu" + Integer.toString(core)
		    + "/cpufreq/cpuinfo_max_freq"));
	    line = reader.readLine();
	    reader.close();
	}
	catch (IOException ex)
	{
	    ex.printStackTrace();
	    return 0;
	}

	try
	{
	    maxFreq = Integer.parseInt(line);
	}
	catch (NumberFormatException ex)
	{
	    ex.printStackTrace();
	    return 0;
	}
	return maxFreq;
    }

    /***
     * Gets the minimum frequency of current cpu core provided as parameter. Requires: Ability to read the system file "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq"
     * 
     * @return The min frequency in Hz or 0 if failed to get result
     */
    public int getMinFreq(int core)
    {
	int minFreq = 0;
	String line = null;
	try
	{
	    BufferedReader reader = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu" + Integer.toString(core)
		    + "/cpufreq/cpuinfo_min_freq"));
	    line = reader.readLine();
	    reader.close();
	}
	catch (IOException ex)
	{
	    ex.printStackTrace();
	    return 0;
	}

	try
	{
	    minFreq = Integer.parseInt(line);
	}
	catch (NumberFormatException ex)
	{
	    ex.printStackTrace();
	    return 0;
	}
	return minFreq;
    }

    /***
     * Gets the current frequency of current cpu core provided as parameter. Requires: Ability to read the system file "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"
     * 
     * @return The current frequency in Hz or 0 if failed to get result
     */
    public int getCurFreq(int core)
    {
	int curFreq = 0;
	String line = null;
	try
	{
	    BufferedReader reader = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu" + Integer.toString(core)
		    + "/cpufreq/scaling_cur_freq"));
	    line = reader.readLine();
	    reader.close();
	}
	catch (IOException ex)
	{
	    ex.printStackTrace();
	    return 0;
	}

	try
	{
	    curFreq = Integer.parseInt(line);
	}
	catch (NumberFormatException ex)
	{
	    ex.printStackTrace();
	    return 0;
	}
	return curFreq;
    }

    /***
     * Gets the version of the Application Binary Interface
     * 
     * @return the version of androideabi (androideabi-v6j for ARMv6)
     */
    public String abi()
    {
	return android.os.Build.CPU_ABI;
    }

    /**
     * Gets the total system memory Opens /proc/meminfo file and reads the first line only (Total memory: xxxxxx kb)
     * 
     * @return total memory in Kb. Returns 0 if there is an error reading the file.
     */
    public String totalRAM()
    {
	RandomAccessFile reader = null;
	String delims = "[ ]+";
	String firstLine = null;
	String[] totalMem = null;

	try
	{
	    reader = new RandomAccessFile("/proc/meminfo", "r");
	    firstLine = reader.readLine();
	    totalMem = firstLine.split(delims); // Get total memory in Kb
	    reader.close();
	    return totalMem[1];
	}
	catch (IOException ex)
	{
	    ex.printStackTrace();
	    return "0"; // Error reading file
	}
    }

    public boolean isSdPresent()
    {
	return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }
}
