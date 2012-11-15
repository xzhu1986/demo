import os.path,boto,os,email.utils,time,gzip
import config

def scan_files(dir, exts):
	for root, dirs, files in os.walk(dir):
		if dir.find('WEB-INF') == -1 and root.find('WEB-INF') != -1:
			continue
		for file in files:
			if accept_file(file, exts):
				yield os.path.join(root, file)

def accept_file(file, exts):
	return len(filter(lambda s: file.lower().endswith('.'+s), exts)) > 0

def upload_file(s3, file, bucket, target, public=False):
	key = bucket.get_key(target)
	if key != None:
		mtime = time.mktime(email.utils.parsedate_tz(key.last_modified)[0:9])
		if mtime >= os.path.getmtime(file):
			if can_compress(file):
				if key.content_encoding!=None:
					return
			else:
				return
	else:
		key = bucket.new_key(target)
	print 'uploading '+file+ ' to s3://'+bucket.name+'/'+target
	if can_compress(file):
		f_in = open(file, 'rb')
		f_out = gzip.open(file+'.gz', 'wb')
		f_out.writelines(f_in)
		f_out.close()
		f_in.close()
		key.metadata.update({'Content-Type':get_content_type(file),'Content-Encoding':'gzip'})
		key.set_contents_from_filename(file+'.gz')
	else:
		key.set_contents_from_filename(file)
	if public==True:
		key.set_acl('public-read')

def can_compress(file):
	type_match=len(filter(lambda s: file.lower().endswith('.'+s), ['txt','html','js','css','htm','json'])) > 0
	if type_match==False:
		return False
	return os.path.getsize(file)>500

def get_content_type(file):
	map={'txt':'text/plain','html':'text/html','htm':'text/html','json':'application/json','css':'text/css','js':'application/x-javascript'}
	return map[file.rsplit('.',1)[1]]

if __name__=="__main__":
	import sys
	if len(sys.argv) < 5:
		print 'usage: upload_static_s3.py <env> <path> <target_bucket> <target_s3_folder> <accept_exts> <public(true/false)>'
		exit()
	path = sys.argv[2]
	bucket_name = sys.argv[3]
	prefix = sys.argv[4]
	access_key=config.get_aws_credentials(sys.argv[1])
	exts = sys.argv[5].split(',')
	is_public = sys.argv[6].lower()=='true'
	s3 = boto.connect_s3(aws_access_key_id=access_key[0],aws_secret_access_key=access_key[1])
	bucket = s3.get_bucket(bucket_name)
	if bucket is None:
		bucket = s3.create_bucket(bucket_name)
	for file in scan_files(path,exts):
		target = prefix+file[len(path):]
		upload_file(s3, file, bucket, target, public=is_public)
