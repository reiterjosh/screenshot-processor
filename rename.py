import os
import glob
import sys
import shutil

if len(sys.argv) <3:
	print("Run with: python rename.py <featureName> <indexLoc> [optional: targetDir]")
	sys.exit(0)

feature = sys.argv[1]
indexLoc = int(sys.argv[2])
refDir = '/Volumes/Personal/Sven/_Babel/en-reference-screens/'+feature
targetDir = '/Users/google/Desktop/Rename'

if len(sys.argv) >= 4: targetDir = sys.argv[3]

if not os.path.exists(refDir):
	print("Cannot find reference screens.")
	sys.exit(0)

if not os.path.exists(targetDir):
	print("Cannot find target screens.")
	sys.exit(0)

outDir = targetDir+"/out"
if not os.path.exists(outDir):
    os.makedirs(outDir)

targetFiles = glob.glob(targetDir+'/*.png')
refs = glob.glob(refDir+'/*.png')

for file in targetFiles:
	found = False
	index = int(os.path.splitext(file)[0].split('_')[indexLoc])

	for ref in refs: 
		# ignore "duplicate.png"
		if "_" not in os.path.basename(ref):
			continue

		refIndex = int(os.path.basename(ref).split('_')[1])

		if index == refIndex:
			newName = os.path.basename(file).split("_")[0]+os.path.basename(ref)[2:]
			newPath = outDir+"/"+newName
			shutil.copy(file, newPath)
			found = True
			continue

	if found == False:
		print("INDEX NOT FOUND:  "+os.path.basename(file))

print("Renaming finished.")