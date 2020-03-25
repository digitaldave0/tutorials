import boto3

#use boto s3
s3 = boto3.resource('s3')
for bucket in s3.buckets.all():
  print(bucket.name)

# Upload a new file from local to s3 bucket ab
data = open('images/Firefox_wallpaper.png', 'rb')
s3.Bucket('dah-test').put_object(Key='Firefox_wallpaper.png', Body=data)

#use boto client library to download file from s3 bucket to local machine

s3client = boto3.client('s3')
s3client.download_file('dah-test', 'Firefox_wallpaper.png', 'images/client_Firefox_wallpaper.png')