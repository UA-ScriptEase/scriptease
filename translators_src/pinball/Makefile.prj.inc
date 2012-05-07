#-----------------------------------------------------------------------------
# GNU Makefile for static libraries: project dependent part
#
# $Id: Makefile.prj.inc,v 1.2 2006/10/20 20:20:15 emarkus Exp $
# $Source: /usr/cvsroot/project_hog/build/gmake/apps/sample/Makefile.prj.inc,v $
#-----------------------------------------------------------------------------

DBG_NAME = $(NAME)
REL_NAME = $(NAME)

ROOT = ../../..
VPATH = $(ROOT)

DBG_OBJDIR = $(ROOT)/objs/$(NAME)/debug
REL_OBJDIR = $(ROOT)/objs/$(NAME)/release
DBG_BINDIR = $(ROOT)/bin/debug
REL_BINDIR = $(ROOT)/bin/release

PROJ_CXXFLAGS = -I$(ROOT)/xmlParser  -I$(ROOT)/controller -I$(ROOT)/eventLibrary

PROJ_DBG_CXXFLAGS = $(PROJ_CXXFLAGS)
PROJ_REL_CXXFLAGS = $(PROJ_CXXFLAGS)

PROJ_DBG_LNFLAGS = -L$(DBG_BINDIR)
PROJ_REL_LNFLAGS = -L$(REL_BINDIR)

PROJ_DBG_LIB = -leventLibrary -lcontroller -lxmlParser 
PROJ_REL_LIB = -leventLibrary -lcontroller -lxmlParser


PROJ_DBG_DEP = \
  $(DBG_BINDIR)/libxmlParser.a \
  $(DBG_BINDIR)/libeventLibrary.a \
  $(DBG_BINDIR)/libcontroller.a

PROJ_REL_DEP = \
  $(DBG_BINDIR)/libxmlParser.a \
  $(DBG_BINDIR)/libeventLibrary.a \
  $(DBG_BINDIR)/libcontroller.a

default : all

SRC_CPP = \
