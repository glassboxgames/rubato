from PIL import Image
import sys

if __name__ == '__main__':
  filename = sys.argv[1]
  print("resizing " + filename)
  im = Image.open(filename)
  (w, h) = im.size
  im.resize((w * 3 // 4, h * 3 // 4)).save(filename)
