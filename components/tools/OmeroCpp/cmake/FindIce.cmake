#.rst:
# FindIce
# -------
#
# Find the ZeroC Internet Communication Engine (ICE) programs,
# libraries and datafiles.
#
# Use this module by invoking find_package with the form::
#
#   find_package(Ice
#     [version] [EXACT]       # Minimum or EXACT version e.g. 3.5.1
#     [REQUIRED]              # Fail with error if Ice is not found
#     [COMPONENTS <libs...>]) # Ice libraries by their name
#
# Components can include any of: ``Freeze``, ``Glacier2``, ``Ice``,
# ``IceBox``, ``IceDB``, ``IceGrid``, ``IcePatch``, ``IceSSL``,
# ``IceStorm``, ``IceUtil``, ``IceXML``, or ``Slice``.
#
# This module reports information about the Ice installation in
# several variables.  General variables::
#
#   Ice_VERSION - Ice release version
#   Ice_FOUND - true if the main programs and libraries were found
#   Ice_LIBRARIES - component libraries to be linked
#   Ice_INCLUDE_DIRS - the directories containing the Ice headers
#   Ice_SLICE_DIRS - the directories containing the Ice slice interface
#                    definitions
#
# Ice programs are reported in::
#
#   Ice_SLICE2CPP_EXECUTABLE - path to slice2cpp executable
#   Ice_SLICE2CS_EXECUTABLE - path to slice2cs executable
#   Ice_SLICE2FREEZEJ_EXECUTABLE - path to slice2freezej executable
#   Ice_SLICE2FREEZE_EXECUTABLE - path to slice2freeze executable
#   Ice_SLICE2HTML_EXECUTABLE - path to slice2html executable
#   Ice_SLICE2JAVA_EXECUTABLE - path to slice2java executable
#   Ice_SLICE2PHP_EXECUTABLE - path to slice2php executable
#   Ice_SLICE2PY_EXECUTABLE - path to slice2py executable
#   Ice_SLICE2RB_EXECUTABLE - path to slice2rb executable
#
# Ice component libraries are reported in::
#
#   Ice_<C>_FOUND - ON if component was found
#   Ice_<C>_LIBRARIES - libraries for component
#
# Note that ``<C>`` is the uppercased name of the component.
#
# This module reads hints about search results from::
#
#   Ice_HOME - the root of the Ice installation
#
# The environment variable :envvar:`ICE_HOME` may also be used; the
# Ice_HOME variable takes precedence.
#
# The following cache variables may also be set::
#
#   Ice_<P>_EXECUTABLE - the path to executable <P>
#   Ice_INCLUDE_DIR - the directory containing the Ice headers
#   Ice_SLICE_DIR - the directory containing the Ice slice interface
#                   definitions
#   Ice_<C>_LIBRARY - the library for component <C>
#
# .. note::
#
#   In most cases none of the above variables will require setting,
#   unless multiple Ice versions are available and a specific version
#   is required.  On Windows, the most recent version of Ice will be
#   found through the registry.  On Unix, the programs, headers and
#   libraries will usually be in standard locations, but Ice_SLICE_DIRS
#   might not be automatically detected (commonly known locations are
#   searched).  All the other variables are defaulted using Ice_HOME,
#   if set.  It's possible to set Ice_HOME and selectively specify
#   alternative locations for the other components; this might be
#   required for e.g. newer versions of Visual Studio if the
#   heuristics are not sufficient to identify the correct programs and
#   libraries for the specific Visual Studio version.
#
# Other variables one may set to control this module are::
#
#   Ice_DEBUG - Set to ON to enable debug output from FindIce.

# Written by Roger Leigh <rleigh@codelibre.net>

#=============================================================================
# Copyright 2014 University of Dundee
#
# Distributed under the OSI-approved BSD License (the "License");
# see accompanying file Copyright.txt for details.
#
# This software is distributed WITHOUT ANY WARRANTY; without even the
# implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the License for more information.
#=============================================================================
# (To distribute this file outside of CMake, substitute the full
#  License text for the above reference.)

# The Ice checks are contained in a function due to the large number
# of temporary variables needed.
function(_Ice_FIND)
  # Released versions of Ice, including generic short forms
  set(ice_versions
      3
      3.5
      3.5.1
      3.5.0
      3.4
      3.4.2
      3.4.1
      3.4.0
      3.3
      3.3.1
      3.3.0)

  # Set up search paths, taking compiler into account.  Search Ice_HOME,
  # with ICE_HOME in the environment as a fallback if unset.
  if(Ice_HOME)
    list(APPEND ice_roots "${Ice_HOME}")
  else(Ice_HOME)
    if("$ENV{ICE_HOME}" STREQUAL "")
    else("$ENV{ICE_HOME}" STREQUAL "")
      file(TO_CMAKE_PATH "$ENV{ICE_HOME}" NATIVE_PATH)
      list(APPEND ice_roots "${NATIVE_PATH}")
      set(Ice_HOME "${NATIVE_PATH}"
          CACHE PATH "Location of the Ice installation" FORCE)
    endif("$ENV{ICE_HOME}" STREQUAL "")
  endif(Ice_HOME)

  if(CMAKE_SIZEOF_VOID_P EQUAL 8)
    # 64-bit path suffix
    set(_x64 "/x64")
    # 64-bit library directory
    set(_lib64 "lib64")
  endif(CMAKE_SIZEOF_VOID_P EQUAL 8)

  if(MSVC_VERSION)
    # VS 8.0
    if((MSVC_VERSION EQUAL 1400) OR (MSVC_VERSION GREATER 1400 AND MSVC_VERSION LESS 1500))
      set(vcver "vc80")
      set(vcyear "2005")
    endif((MSVC_VERSION EQUAL 1400) OR (MSVC_VERSION GREATER 1400 AND MSVC_VERSION LESS 1500))
    # VS 9.0
    if((MSVC_VERSION EQUAL 1500) OR (MSVC_VERSION GREATER 1500 AND MSVC_VERSION LESS 1600))
      set(vcver "vc90")
      set(vcyear "2008")
    endif((MSVC_VERSION EQUAL 1500) OR (MSVC_VERSION GREATER 1500 AND MSVC_VERSION LESS 1600))
    # VS 10.0
    if((MSVC_VERSION EQUAL 1600) OR (MSVC_VERSION GREATER 1600 AND MSVC_VERSION LESS 1700))
      set(vcver "vc100")
    endif((MSVC_VERSION EQUAL 1600) OR (MSVC_VERSION GREATER 1600 AND MSVC_VERSION LESS 1700))
    # VS 11.0
    if((MSVC_VERSION EQUAL 1700) OR (MSVC_VERSION GREATER 1700 AND MSVC_VERSION LESS 1800))
      set(vcver "vc110")
    endif((MSVC_VERSION EQUAL 1700) OR (MSVC_VERSION GREATER 1700 AND MSVC_VERSION LESS 1800))
    # VS 12.0
    if((MSVC_VERSION EQUAL 1800) OR (MSVC_VERSION GREATER 1800 AND MSVC_VERSION LESS 1900))
      set(vcver "vc120")
    endif((MSVC_VERSION EQUAL 1800) OR (MSVC_VERSION GREATER 1800 AND MSVC_VERSION LESS 1900))
    # VS 14.0
    if((MSVC_VERSION EQUAL 1900) OR (MSVC_VERSION GREATER 1900 AND MSVC_VERSION LESS 2000))
      set(vcver "vc140")
    endif((MSVC_VERSION EQUAL 1900) OR (MSVC_VERSION GREATER 1900 AND MSVC_VERSION LESS 2000))
  endif(MSVC_VERSION)

  # For compatibility with ZeroC Windows builds.
  if(vcver)
    # Earlier Ice (3.3) builds don't use vcnnn subdirectories, but are harmless to check.
    list(APPEND ice_binary_suffixes "bin/${vcver}${_x64}" "bin/${vcver}")
    list(APPEND ice_library_suffixes "lib/${vcver}${_x64}" "lib/${vcver}")
  endif(vcver)
  # Generic 64-bit and 32-bit directories
  list(APPEND ice_binary_suffixes "bin${_x64}" "bin")
  list(APPEND ice_library_suffixes "${_lib64}" "lib${_x64}" "lib")
  list(APPEND ice_include_suffixes "include")
  list(APPEND ice_slice_suffixes "slice")

  # On Windows, look in the registry for install locations.  Different
  # versions of Ice install support different compiler versions.
  if(vcver)
    foreach(ice_version ${ice_versions})
      # Ice 3.3 releases use a Visual Studio year suffix and value is
      # enclosed in double quotes, though only the leading quote is
      # returned by get_filename_component.
      unset(ice_location)
      if(vcyear)
        get_filename_component(ice_location
                               "[HKEY_LOCAL_MACHINE\\SOFTWARE\\ZeroC\\Ice ${ice_version} for Visual Studio ${vcyear};InstallDir]"
                               PATH)
        if(ice_location AND NOT ("${ice_location}" STREQUAL "/registry" OR "${ice_location}" STREQUAL "/"))
          string(REGEX REPLACE "^\"(.*)\"?$" "\\1" ice_location "${ice_location}")
          get_filename_component(ice_location "${ice_location}" ABSOLUTE)
        else(ice_location AND NOT ("${ice_location}" STREQUAL "/registry" OR "${ice_location}" STREQUAL "/"))
          unset(ice_location)
        endif(ice_location AND NOT ("${ice_location}" STREQUAL "/registry" OR "${ice_location}" STREQUAL "/"))
      endif(vcyear)
      # Ice 3.4+ releases don't use a suffix
      if(NOT ice_location OR "${ice_location}" STREQUAL "/registry")
        get_filename_component(ice_location
                               "[HKEY_LOCAL_MACHINE\\SOFTWARE\\ZeroC\\Ice ${ice_version};InstallDir]"
                               ABSOLUTE)
      endif(NOT ice_location OR "${ice_location}" STREQUAL "/registry")

      if(ice_location AND NOT "${ice_location}" STREQUAL "/registry")
        list(APPEND ice_roots "${ice_location}")
      endif(ice_location AND NOT "${ice_location}" STREQUAL "/registry")
    endforeach(ice_version)
  else(vcver)
    foreach(ice_version ${ice_versions})
      # Prefer 64-bit variants if present (and using a 64-bit compiler)
      list(APPEND ice_roots "/opt/Ice-${ice_version}")
    endforeach(ice_version)
  endif(vcver)

  set(ice_programs
      slice2cpp
      slice2cs
      slice2freezej
      slice2freeze
      slice2html
      slice2java
      slice2php
      slice2py
      slice2rb)

  # Find all Ice programs
  foreach(program ${ice_programs})
    string(TOUPPER "${program}" program_upcase)
    set(cache_var "Ice_${program_upcase}_EXECUTABLE")
    set(program_var "Ice_${program_upcase}_EXECUTABLE")
    find_program("${cache_var}" "${program}"
      HINTS ${ice_roots}
      PATH_SUFFIXES ${ice_binary_suffixes}
      DOC "Ice ${program} executable")
    mark_as_advanced(cache_var)
    set("${program_var}" "${${cache_var}}" PARENT_SCOPE)
  endforeach(program ${ice_programs})

  # Get version.
  if(Ice_SLICE2CPP_EXECUTABLE)
    # Execute in C locale for safety
    set(_Ice_SAVED_LC_ALL "$ENV{LC_ALL}")
    set(ENV{LC_ALL} C)

    execute_process(COMMAND ${Ice_SLICE2CPP_EXECUTABLE} --version
      ERROR_VARIABLE Ice_VERSION_SLICE2CPP_FULL
      ERROR_STRIP_TRAILING_WHITESPACE)

    # restore the previous LC_ALL
    set(ENV{LC_ALL} ${_Ice_SAVED_LC_ALL})

    # Make short version
    string(REGEX REPLACE "^(.*)\\.[^.]*$" "\\1" Ice_VERSION_SLICE2CPP_SHORT "${Ice_VERSION_SLICE2CPP_FULL}")
    set(Ice_VERSION "${Ice_VERSION_SLICE2CPP_FULL}" PARENT_SCOPE)
  endif(Ice_SLICE2CPP_EXECUTABLE)

  message(STATUS "Ice version: ${Ice_VERSION_SLICE2CPP_FULL}")

  # The following searches prefer the version found; note reverse
  # order due to prepending.
  if(NOT MSVC)
    list(INSERT ice_roots 0 "/opt/Ice-${Ice_VERSION_SLICE2CPP_SHORT}")
    list(INSERT ice_roots 0 "/opt/Ice-${Ice_VERSION_SLICE2CPP_FULL}")
  endif(NOT MSVC)

  # Find include directory
  find_path(Ice_INCLUDE_DIR
            NAMES "Ice/Ice.h"
            HINTS ${ice_roots}
            PATH_SUFFIXES ${ice_include_suffixes}
            DOC "Ice include directory")
  set(Ice_INCLUDE_DIR "${Ice_INCLUDE_DIR}" PARENT_SCOPE)

  # In common use on Linux and MacOS X (homebrew); prefer version-specific dir
  list(APPEND ice_slice_paths
       /usr/local/share /usr/share)
  list(APPEND ice_slice_suffixes
       "Ice-${Ice_VERSION_SLICE2CPP_FULL}/slice"
       "Ice-${Ice_VERSION_SLICE2CPP_SHORT}/slice")

  # Find slice directory
  find_path(Ice_SLICE_DIR
            NAMES "Ice/Connection.ice"
            HINTS ${ice_roots}
                  ${ice_slice_paths}
            PATH_SUFFIXES ${ice_slice_suffixes}
            NO_DEFAULT_PATH
            DOC "Ice slice directory")
  set(Ice_SLICE_DIR "${Ice_SLICE_DIR}" PARENT_SCOPE)

  # Find all Ice libraries
  set(Ice_REQUIRED_LIBS_FOUND ON)
  foreach(component ${Ice_FIND_COMPONENTS})
    string(TOUPPER "${component}" component_upcase)
    set(component_cache "Ice_${component_upcase}_LIBRARY")
    set(component_found "${component_upcase}_FOUND")
    find_library("${component_cache}" "${component}"
      HINTS ${ice_roots}
      PATH_SUFFIXES ${ice_library_suffixes}
      DOC "Ice ${component} library")
    mark_as_advanced("${component_cache}")
    if("${component_cache}")
      set("${component_found}" ON)
      list(APPEND Ice_LIBRARY "${${component_cache}}")
    endif("${component_cache}")
    mark_as_advanced("${component_found}")
    set("${component_cache}" "${${component_cache}}" PARENT_SCOPE)
    set("${component_found}" "${${component_found}}" PARENT_SCOPE)
    if("${component_found}")
      if ("Ice_FIND_REQUIRED_${component}")
        list(APPEND Ice_LIBS_FOUND "${component} (required)")
      else("Ice_FIND_REQUIRED_${component}")
        list(APPEND Ice_LIBS_FOUND "${component} (optional)")
      endif("Ice_FIND_REQUIRED_${component}")
    else("${component_found}")
      if ("Ice_FIND_REQUIRED_${component}")
        set(Ice_REQUIRED_LIBS_FOUND OFF)
        list(APPEND Ice_LIBS_NOTFOUND "${component} (required)")
      else("Ice_FIND_REQUIRED_${component}")
        list(APPEND Ice_LIBS_NOTFOUND "${component} (optional)")
      endif("Ice_FIND_REQUIRED_${component}")
    endif("${component_found}")
  endforeach(component)
  set(_Ice_REQUIRED_LIBS_FOUND "${Ice_REQUIRED_LIBS_FOUND}" PARENT_SCOPE)
  set(Ice_LIBRARY "${Ice_LIBRARY}" PARENT_SCOPE)

  if(Ice_LIBS_FOUND)
    message(STATUS "Found the following Ice libraries:")
    foreach(found ${Ice_LIBS_FOUND})
      message(STATUS "  ${found}")
    endforeach(found)
  endif(Ice_LIBS_FOUND)
  if(Ice_LIBS_NOTFOUND)
    message(STATUS "The following Ice libraries were not found:")
    foreach(notfound ${Ice_LIBS_NOTFOUND})
      message(STATUS "  ${notfound}")
    endforeach(notfound)
  endif(Ice_LIBS_NOTFOUND)

  if(Ice_DEBUG)
    message(STATUS "--------FindIce.cmake search debug--------")
    message(STATUS "ICE binary path search order: ${ice_roots}")
    message(STATUS "ICE include path search order: ${ice_roots}")
    message(STATUS "ICE slice path search order: ${ice_roots} ${ice_slice_paths}")
    message(STATUS "ICE library path search order: ${ice_roots}")
    message(STATUS "----------------")
  endif(Ice_DEBUG)
endfunction(_Ice_FIND)

_Ice_FIND()

include(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(Ice
                                  FOUND_VAR Ice_FOUND
                                  REQUIRED_VARS Ice_SLICE2CPP_EXECUTABLE
                                                Ice_INCLUDE_DIR
                                                Ice_SLICE_DIR
                                                Ice_LIBRARY
                                                _Ice_REQUIRED_LIBS_FOUND
                                  VERSION_VAR Ice_VERSION
                                  FAIL_MESSAGE "Failed to find all Ice components")

unset(_Ice_REQUIRED_LIBS_FOUND)

if(Ice_FOUND)
  set(Ice_INCLUDE_DIRS "${Ice_INCLUDE_DIR}")
  set(Ice_SLICE_DIRS "${Ice_SLICE_DIR}")
  set(Ice_LIBRARIES "${Ice_LIBRARY}")
  foreach(component ${Ice_FIND_COMPONENTS})
    string(TOUPPER "${component}" component_upcase)
    set(component_cache "Ice_${component_upcase}_LIBRARY")
    set(component_lib "Ice_${component_upcase}_LIBRARIES")
    set(component_found "${component_upcase}_FOUND")
    if("${component_found}")
      set("${component_lib}" "${${component_cache}}")
    endif("${component_found}")
  endforeach(component ${Ice_FIND_COMPONENTS})
endif(Ice_FOUND)

if(Ice_DEBUG)
  message(STATUS "--------FindIce.cmake results debug--------")
  message(STATUS "Ice_VERSION number: ${Ice_VERSION}")
  message(STATUS "Ice_HOME directory: ${Ice_HOME}")
  message(STATUS "Ice_INCLUDE_DIR directory: ${Ice_INCLUDE_DIR}")
  message(STATUS "Ice_SLICE_DIR directory: ${Ice_SLICE_DIR}")
  message(STATUS "Ice_LIBRARIES: ${Ice_LIBRARIES}")
  message(STATUS "slice2cpp executable: ${Ice_SLICE2CPP_EXECUTABLE}")
  message(STATUS "slice2cs executable: ${Ice_SLICE2CS_EXECUTABLE}")
  message(STATUS "slice2freezej executable: ${Ice_SLICE2FREEZEJ_EXECUTABLE}")
  message(STATUS "slice2freeze executable: ${Ice_SLICE2FREEZE_EXECUTABLE}")
  message(STATUS "slice2html executable: ${Ice_SLICE2HTML_EXECUTABLE}")
  message(STATUS "slice2java executable: ${Ice_SLICE2JAVA_EXECUTABLE}")
  message(STATUS "slice2php executable: ${Ice_SLICE2PHP_EXECUTABLE}")
  message(STATUS "slice2py executable: ${Ice_SLICE2PY_EXECUTABLE}")
  message(STATUS "slice2rb executable: ${Ice_SLICE2RB_EXECUTABLE}")
  foreach(component ${Ice_FIND_COMPONENTS})
    string(TOUPPER "${component}" component_upcase)
    set(component_lib "Ice_${component_upcase}_LIBRARIES")
    set(component_found "${component_upcase}_FOUND")
    message(STATUS "${component} library found: ${${component_found}}")
    message(STATUS "${component} library: ${${component_lib}}")
  endforeach(component ${Ice_FIND_COMPONENTS})
  message(STATUS "----------------")
endif(Ice_DEBUG)
