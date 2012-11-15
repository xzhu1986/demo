import boto

def upload(bucket, f, prefix):
	k=bucket.new_key(prefix+'/test.txt')
	k.set_content_from_filename(f)

if __name__== "__main__":
	s3 = boto.connect_s3(aws_access_key_id='AKIAI5HCZMWKQJLZUDFA',aws_secret_access_key='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').get_bucket('isell.resource.test')
	upload(s3, '/Users/yezhou/projects/cloud/scripts/test/test.txt', 'data/test_supplier_1')
