import os.path,boto,os,email.utils,time
import config,upload_static_s3

if __name__=="__main__":
	import sys
	if len(sys.argv) < 5:
		print 'usage: upload_s3.py <filename> <target_bucket> <target_s3_folder>'
		exit()
	file = sys.argv[2]
	bucket_name = sys.argv[3]
	prefix = sys.argv[4]
	access_key=config.get_aws_credentials(sys.argv[1])
	s3 = boto.connect_s3(aws_access_key_id=access_key[0],aws_secret_access_key=access_key[1])
	bucket = s3.get_bucket(bucket_name)
	if bucket is None:
		bucket = s3.create_bucket(bucket_name)
	target = prefix
	upload_static_s3.upload_file(s3, file, bucket, target)

