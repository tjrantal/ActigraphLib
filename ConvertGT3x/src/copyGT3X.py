#A script to go through a folder recrusively, and to copy files with a given suffix to another location
#Not finished...

import os
import shutil
import subprocess
import csv
import string

#Functions

def handleSubjectFolder(folderIn,targetFolder):
	fileList = os.listdir(folderIn)
	for file in fileList:
		#if it's a .gt3x file extract it to targetFolder
		if file.endswith('.gt3x'):
			os.makedirs(targetFolder)	#create the target folder
			#os.system('java -cp \".;gt3x.jar\" com.video4coach.DecompressGT3x '+folderIn+'/'+file+' '+targetFolder)
			#print ('java -cp \".;gt3x.jar\" '+folderIn+'/'+file+' '+targetFolder) 
			subprocess.call(['java','-cp','\".;gt3x.jar\"','com.video4coach.DecompressGT3x',folderIn+'/'+file,targetFolder+'/'])
			#print("Extracted "+folderIn+'/'+file)
			#print (['java','-cp','\".;gt3x.jar\"',folderIn+'/'+file,targetFolder]) 

def handleRootFolder(folderIn,targetFolder):
	fileList = os.listdir(folderIn)
	for file in fileList:
		#Skip files that have already been extracted
		if os.path.exists(targetFolder+"/"+file):
			print ("Folder exists "+targetFolder+"/"+file) 
		else:
			if os.path.isdir(folderIn+"/"+file):
				handleSubjectFolder(folderIn+"/"+file,targetFolder+"/"+file)

dataPath = 'S:/Accelerometer/RawData'
targetPath  = 'C:/timo/research/BelavyQuittner2015/data/actigraph'
handleRootFolder(dataPath,targetPath)
	#if os.path.exists('C:'+line[1]+line[0]):
	#	loydetty += 1
	#	print ('Kopioidaan '+line[0])
	#	shutil.copy('C:'+line[1]+line[0],whereTo)
	#subprocess.call(['ls',path+'/'+fname+'/I*.M02',whereTo])
#print('Kopioitiin '+str(loydetty))

