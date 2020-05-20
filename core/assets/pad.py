from PIL import Image
import sys

if __name__ == '__main__':
  filename = sys.argv[1]
  print("padding " + filename)
  im = Image.open(filename)
  im2 = Image.new(im.mode, im.size[0], im.size[1] + 10, (0, 0, 0, 0))
  im2.paste(im, (0, 10)).save(filename)
