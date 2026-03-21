#!/bin/bash

# Clean RuoYi comments from Java files
echo "Cleaning RuoYi comments from Java files..."

find vcc-server -name "*.java" -type f | while read file; do
  # Remove RuoYi copyright headers
  sed -i '' '/Copyright.*RuoYi/d' "$file"
  sed -i '' '/@author.*RuoYi/d' "$file"
  sed -i '' '/@author.*ruoyi/d' "$file"
  sed -i '' '/若依/d' "$file"
  sed -i '' '/ruoyi\.vip/d' "$file"

  # Remove empty @author lines
  sed -i '' '/^[[:space:]]*\*[[:space:]]*@author[[:space:]]*$/d' "$file"

  # Remove empty javadoc blocks (/** */ with only whitespace/asterisks)
  perl -i -0pe 's/\/\*\*\s*\n(\s*\*\s*\n)*\s*\*\/\s*\n//g' "$file"
done

echo "Cleaned Java files in vcc-server"

# Clean console.log from frontend files
echo "Cleaning console.log from frontend files..."

find vcc-web-merchant/src vcc-web-admin/src -name "*.js" -o -name "*.vue" | while read file; do
  sed -i '' '/console\.log/d' "$file"
done

echo "Cleaned console.log from frontend files"
echo "Done!"
