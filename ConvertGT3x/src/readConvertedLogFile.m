%Function to read a binary file created with com.video4coach.LogBinFileReader
%@param fileIn the path and filename of the converted binary file to read
%@return a struct with field channels{1 to 4}, 1 = y, 2 = x, 3 = z, 4 = time stamp (will have the same value for the whole log record)
%Note the UNIX time stamp to date = datestr(unix_time/86400/1000 + datenum(1970,1,1))
function data = readConvertedLogFile(fileIn)
	recordType = {'int16' 'int16' 'int16' 'int32'};
	recordLen = [2 2 2 4]; %
	data = struct();
	fid = fopen(fileIn,'rb');
	data.channels = cell(1,numel(recordType));
	for i=1:numel(recordType)
		% seek to the first field of the first record
		fseek(fid, sum(recordLen(1:i-1)), 'bof');
		% read column with specified format, skipping required number of bytes
		data.channels{i} = double(fread(fid, Inf, ['*' recordType{i}], sum(recordLen)-recordLen(i)));
	end
	fclose(fid);
